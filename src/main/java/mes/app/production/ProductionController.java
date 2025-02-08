package mes.app.production;


import mes.app.production.service.ProductionService;
import mes.domain.entity.User;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile_production")
public class ProductionController {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProductionController.class.getName());
    @Autowired
    ProductionService productionService;

    @GetMapping("/read_all")
    public ResponseEntity<?> getList(@RequestParam(value = "search_startDate", required = false) String startDate,
                                                             @RequestParam(value = "search_endDate", required = false) String endDate,
                                                             @RequestParam(value = "search_property", required = false) String property,
                                     Authentication auth) {

        AjaxResult result = new AjaxResult();
        User user = (User) auth.getPrincipal();
        String userid = String.valueOf(user.getId());

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String formattedStartDate = null;
        String formattedEndDate = null;

        try {
            // 날짜 변환 수행 (null 및 빈 값 체크)
            if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                formattedStartDate = LocalDate.parse(startDate, inputFormatter).format(outputFormatter);
                formattedEndDate = LocalDate.parse(endDate, inputFormatter).format(outputFormatter);
            } else {
                return ResponseEntity.badRequest().body("startDate와 endDate는 필수 입력값입니다.");
            }

            // property 값 검증
            switch (property) {
                case "my_property":
                case "recent_property":
                case "most_viewed_property":
                case "popular_property":
                    break;
                default:
                    return ResponseEntity.badRequest().body("잘못된 property 값입니다.");
            }

            // 로그 추가 (입력 데이터 확인)
            LOGGER.info("요청 데이터 - StartDate: " + formattedStartDate + ", EndDate: " + formattedEndDate + ", Property: " + property);

            // 데이터 조회
            Map<String, Object> productionData = productionService.getlist(formattedStartDate, formattedEndDate, property,userid);

            if (productionData == null || productionData.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("조회된 데이터가 없습니다.");
            }

            return ResponseEntity.ok(productionData);

        } catch (Exception e) {
            LOGGER.severe("데이터 조회 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버에서 데이터를 처리하는 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/pie_chart")
    public AjaxResult getList(Authentication auth){
        AjaxResult result = new AjaxResult();

        User user = (User) auth.getPrincipal();
        String userid = String.valueOf(user.getId());

        List<Map<String, Object>> items = productionService.getSeachList(userid);

        result.data = items;

        return result;
    }

}
