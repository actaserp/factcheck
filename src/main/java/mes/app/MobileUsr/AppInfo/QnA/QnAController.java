package mes.app.MobileUsr.AppInfo.QnA;

import mes.domain.entity.User;
import mes.domain.entity.factcheckEntity.TB_USERQST;
import mes.domain.model.AjaxResult;
import org.exolab.castor.types.DateTime;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/inquiry")
public class QnAController {

    private final QnAService qnAService;

    public QnAController(QnAService qnAService) {
        this.qnAService = qnAService;
    }

    @GetMapping("/list")
    public AjaxResult list(@AuthenticationPrincipal User user){
        AjaxResult result = new AjaxResult();

        List<Map<String, Object>> list = qnAService.selectList(user.getUsername());

        result.success = true;
        result.data = list;
        return result;
    }

    @PostMapping("/save")
    public AjaxResult save(@RequestParam String content,
                           @AuthenticationPrincipal User user){

        AjaxResult result = new AjaxResult();

        Boolean f = qnAService.saveQnA(user, content);

        if(f) result.success = true;

        return result;
    }

    @PostMapping("/delete")
    public AjaxResult delete(@RequestParam Integer id){

        AjaxResult result = new AjaxResult();

        try{
            TB_USERQST tbUserqst = qnAService.QnASearch(id);
            Optional<TB_USERQST> answer = qnAService.AnswerSearchBy(tbUserqst.getQSTSEQ());

            List<TB_USERQST> items = new ArrayList<>();
            answer.ifPresent(items::add);
            items.add(tbUserqst);

            qnAService.deleteQna(items);

            result.success = true;


        }catch(EntityNotFoundException e){
            result.success = false;
            result.message = e.getMessage();
        }catch(Exception e){
            result.success = false;
            result.message = "오류가 발생하였습니다.";
        }

        //Boolean f = qnAService.saveQnA(user, content);

        //if(f) result.success = true;

        return result;
    }


}
