package mes.app.user_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mes.app.user_management.service.UM_userchartService;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/report")
public class UM_userchartContoller {

    @Autowired
    UM_userchartService userchartService;
    @Autowired
    private ObjectMapper objectMapper;
    @GetMapping("/read")
    public AjaxResult getReportList(@RequestParam(value = "startDate",  required = false) String startDate,
                                    @RequestParam(value = "endDate",  required = false) String endDate,
                                    @RequestParam(value = "searchUserNm", required = false) String searchUserNm){
        AjaxResult result = new AjaxResult();
        log.info("들어온 데이터: startDate={}, endDate={}, searchUserNm={}", startDate, endDate, searchUserNm);
        try {

            List<Map<String, Object>> getReportList = userchartService.getGridList(startDate, endDate, searchUserNm);

            result.success = true;
            result.message = "데이터 조회 성공";
            result.data = getReportList;
        } catch (Exception e) {
            log.error("에러 발생: {}", e.getMessage(), e); // 에러 로그 추가
            result.success = false;
            result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
        }

        return result;
    }
}

