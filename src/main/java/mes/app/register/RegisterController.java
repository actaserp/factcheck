package mes.app.register;

import mes.app.register.service.RegisterService;
import mes.app.tilko.service.TilkoService;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/register")
public class RegisterController {

    @Autowired
    RegisterService registerService;

    //등기부등본 상세보기
    @GetMapping("/cardDetail")
    public AjaxResult cardDetail(@RequestParam(value = "REALID")int REALID){
        List<Map<String, Object>> items = new ArrayList<>();
        try {
            items = registerService.cardDetail(REALID);
        }catch (Exception e){
            e.printStackTrace();
        }

        AjaxResult result = new AjaxResult();
        result.data = items;
        return result;
    }
    //등기부등본 점수이력
    @GetMapping("/deductionDetail")
    public AjaxResult deductionDetail(@RequestParam(value = "REALID")int REALID){
        List<Map<String, Object>> items = new ArrayList<>();
        try {
            items = registerService.deductionDetail(REALID);
        }catch (Exception e){
            e.printStackTrace();
        }

        AjaxResult result = new AjaxResult();
        result.data = items;
        return result;
    }
}
