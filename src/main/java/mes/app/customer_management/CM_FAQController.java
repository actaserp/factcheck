package mes.app.customer_management;

import mes.app.customer_management.service.CM_FAQService;
import mes.app.register.service.RegisterService;
import mes.domain.entity.factcheckEntity.TB_FAQINFO;
import mes.domain.model.AjaxResult;
import mes.domain.repository.factcheckRepository.FAQRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
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
    public AjaxResult saveFAQ(@RequestBody TB_FAQINFO faqAnswer) {
        AjaxResult result = new AjaxResult();

        try {
            // Repository를 통해 데이터 저장
            faqRepository.save(faqAnswer);

            result.data = "데이터가 성공적으로 저장되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            result.data = "데이터 저장 중 오류가 발생했습니다.";
        }

        return result;
    }
}
