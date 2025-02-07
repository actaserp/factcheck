package mes.app.MobileUsr.AppInfo.Withdraw;


import mes.app.account.service.TB_USERINFOService;
import mes.app.system.service.UserService;
import mes.domain.entity.User;
import mes.domain.entity.actasEntity.TB_USERINFO;
import mes.domain.model.AjaxResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/withdraw")
public class WithdrawController {

    private final UserService userService;
    private final TB_USERINFOService userinfoService;

    public WithdrawController(UserService userService, TB_USERINFOService userinfoService) {
        this.userService = userService;

        this.userinfoService = userinfoService;
    }

    @Transactional
    @PostMapping("/resign")
    public AjaxResult ResignProc(@RequestParam String id){

        AjaxResult result = new AjaxResult();

        Optional<User> byUsername = userService.findByUsername(id);
        TB_USERINFO userInfo = userinfoService.getUserInfo(id);

        if(byUsername.isPresent() && userInfo != null){

            result.success = userService.withdrawUser(byUsername.get(), userInfo);

            return result;

        }else{
            result.success = false;
            result.message = "해당 사용자가 존재하지 않습니다.";
            return result;
        }

    }

}
