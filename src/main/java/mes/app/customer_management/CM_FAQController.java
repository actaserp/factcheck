package mes.app.customer_management;

import mes.app.customer_management.service.CM_FAQService;
import mes.domain.entity.factcheckEntity.TB_FAQINFO;
import mes.domain.model.AjaxResult;
import mes.domain.repository.factcheckRepository.FAQRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/FAQ")
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
    public AjaxResult saveFAQ( @ModelAttribute TB_FAQINFO faqAnswer) {
        AjaxResult result = new AjaxResult();

        try {
            // Repository를 통해 데이터 저장
            faqRepository.save(faqAnswer);

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

}
