package mes.app.user_management;

import lombok.extern.slf4j.Slf4j;
import mes.app.user_management.service.ClientRegistryService;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/MUserInfo")
public class ClientRegistryController {

    @Autowired
    ClientRegistryService clientRegistryService;


    @GetMapping("/read")
    public AjaxResult getMUserList(@RequestParam(value = "startDate", required = false, defaultValue = "") String startDate,
                                   @RequestParam(value = "endDate", required = false, defaultValue = "") String endDate,
                                   @RequestParam(value = "searchUserNm", required = false, defaultValue = "") String searchUserNm) {
        AjaxResult result = new AjaxResult();
        //log.info("들어온 데이터: startDate={}, endDate={}, searchUserNm={}", startDate, endDate, searchUserNm);

        try {
            // 데이터 조회
            List<Map<String, Object>> getMUserInfoList = clientRegistryService.getList(startDate, endDate, searchUserNm);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

            for (Map<String, Object> user : getMUserInfoList) {
                // 성별 처리
                Object sexYnValue = user.get("sexYN");
                if (sexYnValue != null) {
                    String sexYnStr = sexYnValue.toString();
                    if ("1".equals(sexYnStr)) {
                        user.put("sexYN", "남자");
                    } else if ("2".equals(sexYnStr)) {
                        user.put("sexYN", "여자");
                    } else {
                        user.put("sexYN", "알 수 없음");
                    }
                }

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
            result.data = getMUserInfoList;

        } catch (Exception e) {
            // 예외 처리
            result.success = false;
            result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
        }


        return result;
    }
}