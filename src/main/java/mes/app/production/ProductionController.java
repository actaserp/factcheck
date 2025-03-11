package mes.app.production;


import mes.app.production.service.ProductionService;
import mes.app.tilko.service.TilkoService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/mobile_production")
public class ProductionController {
    private static final Logger LOGGER = Logger.getLogger(ProductionController.class.getName());
    @Autowired
    ProductionService productionService;
    @Autowired
    TilkoService tilkoService;

    @GetMapping("/read_all")
    public ResponseEntity<?> getList(@RequestParam(value = "search_startDate", required = false) String startDate,
                                                             @RequestParam(value = "search_endDate", required = false) String endDate,
                                                             @RequestParam(value = "search_property", required = false) String property,
                                     @RequestParam(value = "keyword", required = false) String keyword,
                                     Authentication auth) {

        AjaxResult result = new AjaxResult();
        User user = (User) auth.getPrincipal();
       /* String userid = String.valueOf(user.getId());*/
        String username = String.valueOf(user.getUsername());


        /*System.out.println("userid확인 "+userid);*/
        System.out.println("username확인 "+username);



     /*   DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String formattedStartDate = null;
        String formattedEndDate = null;*/

        try {
            // 날짜 변환 수행 (null 및 빈 값 체크)
            if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
              /*  formattedStartDate = LocalDate.parse(startDate, inputFormatter).format(outputFormatter);
                formattedEndDate = LocalDate.parse(endDate, inputFormatter).format(outputFormatter);*/
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
            } else {
                return ResponseEntity.badRequest().body("startDate와 endDate는 필수 입력값입니다.");
            }




            // 로그 추가 (입력 데이터 확인)
            LOGGER.info("요청 데이터 - StartDate: " + startDate + ", EndDate: " + endDate + ", Property: " + property);

            // 데이터 조회
            Map<String, Object> productionData = productionService.getlist(startDate, endDate, property,username,keyword);

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
    public AjaxResult getList( @RequestParam(value = "SearchKeywords", required = false) String keyword,
            Authentication auth){
        AjaxResult result = new AjaxResult();

        User user = (User) auth.getPrincipal();
        String userid = String.valueOf(user.getUsername());

        List<Map<String, Object>> items = productionService.getSeachList(userid,keyword);
        // 점수 등급 계산 로직
        // 등급 관련 데이터(S등급 등) 데이터 불러오기
        List<Map<String, Object>> gradeData = tilkoService.getGradeData();
        String Grade = "";
        String GrColor = "";
        for (Map<String, Object> item : items) {
            // 점수
            int realScore = (int) item.get("REALSCORE");
            // **등급 설정**
            for (Map<String, Object> grade : gradeData) {
                int maxScore = (Integer) grade.get("GRSCORE01");
                int minScore = (Integer) grade.get("GRSCORE02");

                if (minScore <= realScore && maxScore >= realScore) {
                    Grade = grade.get("GRID").toString();
                    GrColor = grade.get("GRCOLOR").toString();
                    break;
                }
            }
            if (Grade.isEmpty()) {
                Grade = "F";
            }
            item.put("GRADE", Grade);
            item.put("GRCOLOR", GrColor);
        }

        result.data = items;

        return result;
    }
    // pie chart 요소 조회(차감요인)
    @GetMapping("/pie_chart_data")
    public AjaxResult getDeductionData(@RequestParam(value = "REALID", required = false) String REALID,
                                       @RequestParam(value = "REALSCORE", required = false) int REALSCORE,
                               Authentication auth){
        AjaxResult result = new AjaxResult();

        User user = (User) auth.getPrincipal();
        String userid = String.valueOf(user.getUsername());

        List<Map<String, Object>> items = productionService.getDeductionData(REALID);
        // 점수 등급 계산 로직
        // 등급 관련 데이터(S등급 등) 데이터 불러오기
        List<Map<String, Object>> gradeData = tilkoService.getGradeData();
        // **등급 설정**
        String Grade = "";
        for (Map<String, Object> grade : gradeData) {
            int maxScore = (Integer) grade.get("GRSCORE01");
            int minScore = (Integer) grade.get("GRSCORE02");

            if (minScore <= REALSCORE && maxScore >= REALSCORE) {
                Grade = grade.get("GRID").toString();
                break;
            }
        }

        if (Grade.isEmpty()) {
            Grade = "F";
        }
        Map<String, Object> resultdata = new HashMap<>();
        resultdata.put("data", items);
        resultdata.put("grade", Grade);
        resultdata.put("score", REALSCORE);
        result.data = resultdata;

        return result;
    }
    // 카드정보 조회
    @GetMapping("/getCard")
    public AjaxResult getCard(@RequestParam(value = "REALID", required = false) String REALID,
                              Authentication auth){
        AjaxResult result = new AjaxResult();
        Map<String, Object> resultMap = new HashMap<>();

        User user = (User) auth.getPrincipal();

        // 기존 유저 조회정보에서 동일한 고유번호가 있는지 확인후 없다면 통신 있다면 자료 가져오기
        String Grade = "";
        // 등급 관련 데이터(S등급 등) 데이터 불러오기
        List<Map<String, Object>> gradeData = tilkoService.getGradeData();
        // 저장되어있는 REALID 기반 COMMENT및 차감요소 불러오기
        List<Map<String, Object>> savedDeduction = tilkoService.getDeduction(REALID);
        Map<String, Object> savedGoyu = tilkoService.getRealinfo(REALID);

        int realScore = (int) savedGoyu.get("REALSCORE");

        List<Map<String, Object>> comments = new ArrayList<>();
        if (savedDeduction != null && !savedDeduction.isEmpty()) {  // ✅ null 체크 및 빈 리스트 방지
            for (Map<String, Object> item : savedDeduction) {
                if (item.get("REMARK") != null) {
                    Map<String, Object> cardData = new HashMap<>();
                    cardData.put("DATE", item.get("HISDATE"));
                    cardData.put("REGREMARK", item.get("REMARK"));
                    comments.add(cardData);
                }
            }
        }
        // **등급 설정**
        for (Map<String, Object> grade : gradeData) {
            int maxScore = (Integer) grade.get("GRSCORE01");
            int minScore = (Integer) grade.get("GRSCORE02");

            if (minScore <= realScore && maxScore >= realScore) {
                Grade = grade.get("GRID").toString();
                break;
            }
        }

        if (Grade.isEmpty()) {
            Grade = "F";
        }
        result.message = "기존 조회데이터가 존재합니다.";
        resultMap.put("REALID", savedGoyu.get("REALID"));
        resultMap.put("REALSCORE", savedGoyu.get("REALSCORE"));
        resultMap.put("GRADE", Grade);
        resultMap.put("COMMENT", comments);
        resultMap.put("REGASNAME", savedGoyu.get("REGASNAME"));
        resultMap.put("ADDRESS", savedGoyu.get("REALADD"));
        result.data = resultMap;
        return result;
    }

}
