package mes.app.user_management;

import lombok.extern.slf4j.Slf4j;
import mes.app.user_management.service.MembershipVisitDataService;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RestController
@RequestMapping("/api/userStatistics")
public class MembershipVisitDataContoller {

    @Autowired
    MembershipVisitDataService userStatisticsService;

    @GetMapping("/chart1")
    public AjaxResult getUserStatisticsChart1(@RequestParam(value = "startDate") String startDate,
                                              @RequestParam(value = "endDate") String endDate) {
        AjaxResult result = new AjaxResult();
        log.info("들어온 데이터: startDate={}, endDate={}", startDate, endDate);
        try {
            // 데이터 조회
            List<Map<String, Object>> rawData = userStatisticsService.getList(startDate, endDate);

            // 월별 데이터를 초기화 (1월 ~ 12월)
            List<String> labels = IntStream.rangeClosed(1, 12)
                    .mapToObj(i -> i + "월")
                    .collect(Collectors.toList());
            List<Integer> values = new ArrayList<>(Collections.nCopies(12, 0)); // 초기 값 0

            // SQL 결과를 처리하여 해당 월에 데이터 추가
            for (Map<String, Object> row : rawData) {
                String registerMonth = (String) row.get("RegisterMonth"); // yyyy-MM
                int count = ((Number) row.get("RegisterCount")).intValue();
                int month = Integer.parseInt(registerMonth.split("-")[1]); // MM 추출
                values.set(month - 1, count); // 월별 데이터 설정
            }

            // 반환 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("labels", labels);
            response.put("values", values);

            // 결과 설정
            result.success = true;
            result.message = "데이터 조회 성공";
            result.data = response;

        } catch (Exception e) {
            // 예외 처리
            result.success = false;
            result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
        }

        return result;
    }

    @GetMapping("/chart2")
    public AjaxResult getViewCountChart2(@RequestParam(value = "startDate") String startDate,
                                         @RequestParam(value = "endDate") String endDate) {
        AjaxResult result = new AjaxResult();
        //log.info("들어온 데이터: startDate={}, endDate={}", startDate, endDate);

        try {
            // 데이터 조회
            List<Map<String, Object>> rawData = userStatisticsService.getViewCountChart(startDate, endDate);
            //log.info("받아온 데이터: {}", rawData);

            // 월별 데이터를 초기화 (1월 ~ 12월)
            List<String> labels = IntStream.rangeClosed(1, 12)
                    .mapToObj(i -> i + "월")
                    .collect(Collectors.toList());
            List<Integer> values = new ArrayList<>(Collections.nCopies(12, 0)); // 초기 값 0

            // SQL 결과를 처리하여 해당 월에 데이터 추가
            for (Map<String, Object> row : rawData) {
                String registerMonth = (String) row.get("YearMonth"); // 컬럼 이름 확인
                if (registerMonth == null) {
                    log.warn("YearMonth 값이 null입니다. row: {}", row);
                    continue; // null 값을 무시하고 다음 데이터로 이동
                }

                Object countObject = row.get("TotalInquiryCount"); // 컬럼 이름 확인
                int count = (countObject instanceof Number) ? ((Number) countObject).intValue() : 0;

                int month = Integer.parseInt(registerMonth.split("-")[1]); // MM 추출
                values.set(month - 1, count); // 월별 데이터 설정
            }

            // 반환 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("labels", labels);
            response.put("values", values);

            // 결과 설정
            result.success = true;
            result.message = "데이터 조회 성공";
            result.data = response;

            // 반환 데이터 로그 출력
            log.info("응답 데이터: labels={}, values={}", labels, values);

        } catch (Exception e) {
            // 예외 처리
            log.error("데이터 조회 중 오류 발생", e);
            result.success = false;
            result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
        }

        return result;
    }


    @GetMapping("/read")
    public AjaxResult getUserStatisticsList(@RequestParam(value = "startDate") String startDate,
                                            @RequestParam(value = "endDate") String endDate){
        AjaxResult result = new AjaxResult();

        try{

            List<Map<String, Object>> getUserStatisticsList = userStatisticsService.getGridList(startDate, endDate);

            // 데이터가 있을 경우 성공 메시지
            result.success = true;
            result.message = "데이터 조회 성공";
            result.data = getUserStatisticsList;

        } catch (Exception e) {
            // 예외 처리
            result.success = false;
            result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
        }

        return result;
    }

}
