package mes.app.dashboard;

import lombok.extern.slf4j.Slf4j;
import mes.app.dashboard.service.FactCheckDashBoardService;
import mes.app.system.SystemLogController;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/factDashboard")
public class FactCheckDashBoardController {

    @Autowired
    private FactCheckDashBoardService factService;
    @Autowired
    private SystemLogController systemLogController;

    // 금일 신규 사용자
    @GetMapping("/todaySignup")
    private AjaxResult todaySignup() {
        AjaxResult result = new AjaxResult();
        try{
            List<Map<String, Object>> SignupList = factService.todaySignup();
            for(Map<String, Object> item : SignupList) {
                item.put("no", SignupList.indexOf(item) + 1);
                String indatem = new SimpleDateFormat("yyyy-MM-dd").format(item.get("INDATEM"));
                item.remove("INDATEM");
                item.put("INDATEM", indatem);
            }
            result.data = SignupList;
            log.info("SignupList : {}", SignupList);

            result.message = "신규 사용자 조회에 성공했습니다.";
        }catch (Exception e){
            e.printStackTrace();
            result.message = "신규 사용자 조회에 실패했습니다.";
        }
        return result;
    }
    // 금일 상위 검색매물
    @GetMapping("/todayAddress")
    private AjaxResult todayAddress() {
        AjaxResult result = new AjaxResult();
        try{
            List<Map<String, Object>> AddressList = factService.todayAddress();
            for(Map<String, Object> item : AddressList) {
                item.put("no", AddressList.indexOf(item) + 1);
                String indatem = new SimpleDateFormat("yyyy-MM-dd").format(item.get("INDATEM"));
                item.remove("REQDATE");
                item.put("REQDATE", indatem);
            }
            result.data = AddressList;
            result.message = "검색지역 조회에 성공했습니다.";
        }catch (Exception e){
            e.printStackTrace();
            result.message = "검색지역 조회에 실패했습니다.";
        }
        return result;
    }
    // 1:1 미답변 문의글
    @GetMapping("/QnAList")
    private AjaxResult QnAList() {
        AjaxResult result = new AjaxResult();
        try{
            List<Map<String, Object>> QnAList = factService.QnAList();
            for(Map<String, Object> item : QnAList) {
                item.put("no", QnAList.indexOf(item) + 1);
                // 날자 부분만 추출
                String indatem = new SimpleDateFormat("yyyy-MM-dd").format(item.get("INDATEM"));
                // 시간 부분만 추출
                String time = new SimpleDateFormat("HH시 mm분 ss초").format(item.get("INDATEM"));
                item.remove("INDATEM");
                item.put("INDATEM", indatem);
                item.put("INDATEMTIME", time);
            }
            result.data = QnAList;
            result.message = "미답변 문의글 조회에 성공했습니다.";
        }catch (Exception e){
            e.printStackTrace();
            result.message = "미답변 문의글 조회에 실패했습니다.";
        }
        return result;
    }
    // 차트데이터
    @GetMapping("/createCharts")
    private AjaxResult createCharts() {
        AjaxResult result = new AjaxResult();
        try{
            // 일주일 가입자현황
            List<Map<String, Object>> SignupOfWeek = factService.SignupOfWeek();
            // 유출자현황
            List<Map<String, Object>> Signout = factService.Signout();
            // 등기열람건수
            List<Map<String, Object>> CntOfRegister = factService.CntOfRegister();
            // 상위검색지역
            List<Map<String, Object>> BestSearch = factService.BestSearch();

            Map<String, Object> items = new HashMap<String, Object>();
            items.put("SignupOfWeek", SignupOfWeek);
            items.put("Signout", Signout);
            items.put("CntOfRegister", CntOfRegister);
            items.put("BestSearch", BestSearch);
            result.data = items;
            result.message = "차트데이터 조회에 성공했습니다.";
        }catch (Exception e){
            e.printStackTrace();
            result.message = "차트데이터 조회에 실패했습니다.";
        }
        return result;
    }
    // 카드 데이터
    @GetMapping("/cardDatas")
    private AjaxResult cardDatas() {
        AjaxResult result = new AjaxResult();
        try{
            // 이번달 총 가입자수 집계
            List<Map<String, Object>> CardList = factService.cardDatas();
            // 미답변 문의건 수 집계(YESTERDAY_UNANSWERED_COUNT = YUC ..)
            List<Map<String, Object>> NotAnswerQnAList = factService.NotAnswerQnAList();

            Map<String, Object> items = new HashMap<String, Object>();
            items.put("CardList", CardList);
            items.put("NotAnswerQnAList", NotAnswerQnAList);
            result.data = items;
            result.message = "신규 사용자 조회에 성공했습니다.";
        }catch (Exception e){
            e.printStackTrace();
            result.message = "신규 사용자 조회에 실패했습니다.";
        }
        return result;
    }
}
