package mes.app.user_management;

import lombok.extern.slf4j.Slf4j;
import mes.app.user_management.service.MembershipVisitDataService;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
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

           // 년도별 데이터를 저장할 맵
           Map<String, List<Integer>> yearDataMap = new HashMap<>();

           // 월별 데이터를 초기화 (1월 ~ 12월, 년도별)
           for (Map<String, Object> row : rawData) {
               String registerYearMonth = (String) row.get("RegisterMonth"); // yyyy-MM
               int count = ((Number) row.get("RegisterCount")).intValue();

               // 년도와 월을 분리
               String year = registerYearMonth.split("-")[0]; // yyyy
               int month = Integer.parseInt(registerYearMonth.split("-")[1]); // MM

               // 해당 년도의 월별 데이터를 초기화 (없으면 추가)
               yearDataMap.putIfAbsent(year, new ArrayList<>(Collections.nCopies(12, 0)));

               // 월별 데이터 설정
               yearDataMap.get(year).set(month - 1, count);
           }

           // 결과를 `datasets` 형태로 변환
           List<Map<String, Object>> datasets = new ArrayList<>();
           for (Map.Entry<String, List<Integer>> entry : yearDataMap.entrySet()) {
               String year = entry.getKey();
               List<Integer> values = entry.getValue();

               // 각 데이터셋 구성
               Map<String, Object> dataset = new HashMap<>();
               dataset.put("year", year);
               dataset.put("values", values);
               datasets.add(dataset);
           }

           // 반환 데이터 구성
           Map<String, Object> response = new HashMap<>();
           response.put("labels", IntStream.rangeClosed(1, 12).mapToObj(i -> i + "월").collect(Collectors.toList())); // X축 레이블
           response.put("datasets", datasets); // Y축 데이터셋

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

        try {
            List<Map<String, Object>> rawData = userStatisticsService.getViewCountChart(startDate, endDate);

            // 월별 레이블 초기화
            List<String> labels = IntStream.rangeClosed(1, 12)
                    .mapToObj(i -> i + "월")
                    .collect(Collectors.toList());

            // 데이터셋 초기화
            Map<String, List<Integer>> yearDataMap = new HashMap<>();

            for (Map<String, Object> row : rawData) {
                String registerMonth = (String) row.get("YearMonth");
                if (registerMonth == null) {
                    log.warn("YearMonth 값이 null입니다. row: {}", row);
                    continue;
                }

                Object countObject = row.get("TotalInquiryCount");
                int count = (countObject instanceof Number) ? ((Number) countObject).intValue() : 0;

                String year = registerMonth.split("-")[0]; // yyyy 추출
                int month = Integer.parseInt(registerMonth.split("-")[1]); // MM 추출

                // 해당 년도의 데이터 초기화
                yearDataMap.putIfAbsent(year, new ArrayList<>(Collections.nCopies(12, 0)));

                // 월별 데이터 설정
                yearDataMap.get(year).set(month - 1, count);
            }

            // 프런트엔드에서 기대하는 형식으로 변환
            List<Map<String, Object>> datasets = yearDataMap.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> dataset = new HashMap<>();
                        dataset.put("year", entry.getKey()); // 년도
                        dataset.put("values", entry.getValue()); // 월별 데이터
                        return dataset;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("labels", labels); // X축 레이블
            response.put("datasets", datasets); // Y축 데이터셋

            result.success = true;
            result.message = "데이터 조회 성공";
            result.data = response;

        } catch (Exception e) {
            log.error("데이터 조회 중 오류 발생", e);
            result.success = false;
            result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
        }

        return result;
    }

    @GetMapping("/read")
    public AjaxResult getUserStatisticsList2(@RequestParam(value = "startDate") String startDate,
                                            @RequestParam(value = "endDate") String endDate){
        AjaxResult result = new AjaxResult();

        try{

            List<Map<String, Object>> getUserStatisticsList = userStatisticsService.getGridList(startDate, endDate);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

            for (Map<String, Object> user : getUserStatisticsList) {

                // 날짜 포맷 처리
                Object indatemValue = user.get("indatem");
                if (indatemValue != null) {
                    try {
                        // 날짜를 포맷하여 다시 user 맵에 저장
                        String formattedDate = dateFormatter.format(indatemValue);
                        user.put("indatem", formattedDate);
                    } catch (Exception ex) {
                        // 포맷 오류 처리
                        log.error("날짜 포맷 중 오류 발생: {}", ex.getMessage());
                        user.put("indatem", "잘못된 날짜 형식");
                    }
                }
                Object inquiryCountValue = user.get("InquiryCount");
                if (inquiryCountValue != null) {
                    try {
                        String inquiryCountWithUnit = inquiryCountValue.toString() + " 건";
                        user.put("InquiryCount", inquiryCountWithUnit);
                    } catch (Exception ex) {
                        log.error("조회 건수 처리 중 오류 발생: {}", ex.getMessage());
                        user.put("InquiryCount", "0 건"); // 기본값 설정
                    }
                }
            }
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



    @GetMapping("/read2")
    public AjaxResult getUserStatisticsList(@RequestParam(value = "startDate") String startDate,
                                            @RequestParam(value = "endDate") String endDate){
        AjaxResult result = new AjaxResult();

        try{

            List<Map<String, Object>> getUserStatisticsList = userStatisticsService.getGridList2(startDate, endDate);

            for (Map<String, Object> user : getUserStatisticsList) {

                // 날짜 포맷 처리
                Object REQDATEValue = user.get("issueDate");
                if (REQDATEValue != null) {
                    try {
                        // `issueDate`가 String 형식인 경우 처리
                        Date date = new SimpleDateFormat("yyyyMMdd").parse(REQDATEValue.toString());
                        String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
                        user.put("issueDate", formattedDate);
                    } catch (Exception ex) {
                        // 포맷 오류 처리
                        log.error("날짜 포맷 중 오류 발생: {}", ex.getMessage());
                        user.put("issueDate", "잘못된 날짜 형식");
                    }
                }

                // 조회 건수 처리
                Object inquiryCountValue = user.get("TotalCount");
                if (inquiryCountValue != null) {
                    try {
                        String inquiryCountWithUnit = inquiryCountValue.toString() + " 건";
                        user.put("TotalCount", inquiryCountWithUnit);
                    } catch (Exception ex) {
                        log.error("조회 건수 처리 중 오류 발생: {}", ex.getMessage());
                        user.put("TotalCount", "0 건"); // 기본값 설정
                    }
                }
            }

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
