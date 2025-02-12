package mes.app.MobileUsr.AppInfo.Withdraw;


import mes.app.account.service.TB_USERINFOService;
import mes.app.system.service.UserService;
import mes.config.OneSignalService;
import mes.domain.entity.User;
import mes.domain.entity.actasEntity.TB_USERINFO;
import mes.domain.model.AjaxResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@RestController
@RequestMapping("/api/withdraw")
public class WithdrawController {

    private final UserService userService;
    private final TB_USERINFOService userinfoService;
    private final OneSignalService oneSignalService;


    public WithdrawController(UserService userService, TB_USERINFOService userinfoService, OneSignalService oneSignalService) {
        this.userService = userService;

        this.userinfoService = userinfoService;
        this.oneSignalService = oneSignalService;
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

    @PostMapping("/delete/{userId}")
    public String deleteUser(@PathVariable String userId){

        User user = userService.findByUsername(userId).orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾지 못했습니다."));
        String pushId = "e6a2ca27-1892-41fd-b4ba-b8c023eb3db2";

        System.out.println("진입");

        oneSignalService.sendPushNotification(pushId, "회원을 찾을수 업슷ㅂ니다.");
        return "회원정보를 찾지 못했습니다.";
    }




}
