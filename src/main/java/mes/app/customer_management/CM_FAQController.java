package mes.app.customer_management;

import mes.app.customer_management.service.CM_FAQService;
import mes.domain.entity.User;
import mes.domain.entity.factcheckEntity.TB_FAQINFO;
import mes.domain.model.AjaxResult;
import mes.domain.repository.factcheckRepository.FAQRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/FAQ")
public class CM_FAQController {

    @Autowired
    CM_FAQService faqService;

    @Autowired
    FAQRepository faqRepository;

    // 문의 리스트
    @GetMapping("/getFAQList")
    public AjaxResult getFAQList(){
        List<Map<String, Object>> items = faqService.getFAQList();

        for(Map<String, Object> item : items){
            item.put("no", items.indexOf(item)+1);
        }
        AjaxResult result = new AjaxResult();
        result.data = items;
        return result;
    }
    // 문의/답변 등록
    @PostMapping("/save")
    public AjaxResult saveFAQ(@RequestParam Map<String, String> params,
                              Authentication auth) {
        AjaxResult result = new AjaxResult();
        User user = (User) auth.getPrincipal();

        try {
            // 문의글 등록/수정 로직
            TB_FAQINFO faqQuestion = new TB_FAQINFO();
            if(params.get("FAQSEQ") != null && !params.get("FAQSEQ").isEmpty()) {
                faqQuestion.setFAQSEQ(Integer.valueOf(params.get("FAQSEQ")));
            }
            faqQuestion.setFAQTEXT(params.get("question"));
            faqQuestion.setFLAG("0");
            faqQuestion.setFAQFLAG(params.get("FAQFLAG"));
            faqQuestion.setFASORT(Integer.parseInt(params.get("FAQSORT")));
            faqQuestion.setREMARK(params.get("REMARK"));
            faqQuestion.setINDATEM(LocalDate.parse(params.get("INDATEM")));
            faqQuestion.setINUSERID(user.getUsername());
            // Repository를 통해 데이터 저장
            faqRepository.save(faqQuestion);

            // 답변글 등록/수정 로직 (문의글 id가 넘어왔을 경우만 / 문의글 하위 개념)
            if(params.get("FAQSEQ") != null && !params.get("FAQSEQ").isEmpty()) {
                TB_FAQINFO faqAnswer = new TB_FAQINFO();
                if (params.get("answerNo") != null && !params.get("answerNo").isEmpty()) {
                    faqAnswer.setFAQSEQ(Integer.valueOf(params.get("answerNo")));
                }
                faqAnswer.setFAQTEXT(params.get("answer"));
                faqAnswer.setFLAG("1");
                faqAnswer.setINDATEM(LocalDate.parse(params.get("INDATEM")));
                faqAnswer.setCHSEQ(Integer.valueOf(params.get("CHSEQ")));
                faqAnswer.setINUSERID(user.getUsername());

                faqRepository.save(faqAnswer);
            }
            result.message = "답변이 성공적으로 저장되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            result.message = "답변 저장 중 오류가 발생했습니다.";
        }

        return result;
    }
    @PostMapping("/delete")
    public AjaxResult deleteFAQ(@RequestBody Map<String, Integer> requestData) {
        Integer faqseq = requestData.get("faqseq");
        AjaxResult result = new AjaxResult();
        if (faqseq == null) {
            result.message = "인식번호가 전달되지 않았습니다.";
            return result;
        }

        try {
            faqService.deleteFAQ(faqseq);
            result.message = "답변내용이 성공적으로 삭제되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            result.message = "답변내용 삭제 중 오류가 발생했습니다.";
        }

        return result;
    }
    @GetMapping("/selectMaxFasort")
    public AjaxResult selectMaxFasort() {
        AjaxResult result = new AjaxResult();

        try {
            result.data = faqService.selectMaxFasort();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
