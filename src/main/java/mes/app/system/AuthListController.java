package mes.app.system;


import mes.app.UtilClass;
import mes.app.system.service.AuthListService;
import mes.app.system.service.UserService;
import mes.domain.entity.TB_RP940;
import mes.domain.entity.User;
import mes.domain.model.AjaxResult;
import mes.domain.repository.TB_RP940Repository;
import mes.domain.repository.TB_RP945Repository;
import mes.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/system/auth_list")
public class AuthListController {

    @Autowired
    AuthListService authListService;

    @Autowired
    TB_RP940Repository tb_rp940Repository;

    @Autowired
    TB_RP945Repository tb_rp945Repository;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/read")
    public AjaxResult getList(@RequestParam(value = "searchusr", required = false) String searchusr,
                              @RequestParam(value = "searchfrdate", required = false) String searchfrdate,
                              @RequestParam(value = "searchtodate", required = false) String searchtodate,
                              @RequestParam(value = "searchflag", required = false) String flag,
                              @RequestParam(value = "searchuserid", required = false) String searchuserid) throws ParseException {
        List<Map<String, Object>> items = new ArrayList<>();

        searchusr = Optional.ofNullable(searchusr).orElse("");
        searchfrdate = Optional.ofNullable(searchfrdate).orElse("20000101");
        searchtodate = Optional.ofNullable(searchtodate).orElse("29991231");
        searchuserid = Optional.ofNullable(searchuserid).orElse("");


        if(searchfrdate.isEmpty()){
            searchfrdate = "20000101";
        }
        if(searchtodate.isEmpty()){
            searchtodate = "29991231";
        }

        Timestamp startTime = convertToTimestamp(searchfrdate.replaceAll("-","") + "000000");
        Timestamp endTime = convertToTimestamp(searchtodate.replaceAll("-","") + "235959");



        items = authListService.getAuthList(searchusr, startTime, endTime, flag, searchuserid);

        // Timestamp 값을 String으로 변환
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");
        for (Map<String, Object> item : items) {
            Object appdatem = item.get("appdatem");
            if (appdatem instanceof Timestamp) {
                String formattedDate = dateFormat.format((Timestamp) appdatem);
                item.put("appdatem", formattedDate.substring(0,19));
            }

            Object askdatem = item.get("askdatem");
            if(askdatem instanceof Timestamp){
                String formattedDate = dateFormat.format((Timestamp) askdatem);
                item.put("askdatem", formattedDate.substring(0,19));

            }
        }



        AjaxResult result = new AjaxResult();
        result.data = items;

        return result;
    }


    @GetMapping("/tb_rp945List")
    public AjaxResult getTB_RP945(@RequestParam String userid){

        AjaxResult result = new AjaxResult();

        List<Map<String, Object>> items = authListService.getAuthspList(userid);

        result.data = items;

        return result;
    }

    @PostMapping("/save")
    @Transactional
    public AjaxResult saveFilter(
            @RequestParam(value = "userid") String userid,
            @RequestParam(value = "appflag") String appflag,
            @RequestParam(value = "authgrpcd") String authgrpcd,
            @RequestParam(value = "authgrpnm") String authgrpnm,
            Authentication auth
    ){
        AjaxResult result = new AjaxResult();



        // 현재 시간을 Asia/Seoul 시간대로 가져오기
        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        OffsetDateTime offsetDateTime = seoulDateTime.toOffsetDateTime();



        boolean username_chk = this.userRepository.findIsActiveUserByUsername(userid).isEmpty();


        Timestamp today = new Timestamp(System.currentTimeMillis());
        try{
            if(appflag.equals("Y")){

                if (username_chk == false) {
                    result.success = false;
                    result.message="중복된 사번이 존재합니다.";
                    return result;
                }



                Optional<TB_RP940> TB940list = tb_rp940Repository.findByUserid(userid);
                if(TB940list.isPresent()){
                    TB_RP940 tb940 = TB940list.get();
                    User user = createUserFromTB940(tb940, today);

                    userService.SaveUser(user, auth, tb940.getAuthgrpcd(), tb940.getAuthgrpnm());
                    tb_rp940Repository.updateApprflagToYByUserid(userid, offsetDateTime, authgrpcd, authgrpnm);

                }else {
                    result.success = false;
                    result.message = "신청목록에 없습니다.";
                    return result;
                }



                tb_rp940Repository.updateApprflagToYByUserid(userid, offsetDateTime, authgrpcd, authgrpnm);

            }else{
                tb_rp940Repository.updateApprflagToYByUserid(userid, appflag, authgrpcd, authgrpnm);

                userRepository.deleteByUsername(userid);

            }

            result.success = true;
            result.message = "저장하였습니다.";
            return result;

        }catch(Exception e){
            System.out.println(e);

            result.success = false;
            result.message = "에러가발생하였습니다.";
            return result;
        }


    }

    private static Timestamp convertToTimestamp(String dateTimeString) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        Date parseDate = inputFormat.parse(dateTimeString);

        return new Timestamp(parseDate.getTime());

    }


    @PostMapping("/delete")
    @Transactional
    public AjaxResult delete(@RequestParam(value = "userid") String userid){
        AjaxResult result = new AjaxResult();


        List<String> paramList = new UtilClass().parseUserIds(userid);

        for(String param : paramList){

            Optional<User> user = userRepository.findIsActiveUserByUsername(param);
            System.out.println(user);
            if(user.isPresent()){
                userRepository.deleteByUsername(param);
            }

            tb_rp940Repository.deleteByUserid(param);
            tb_rp945Repository.deleteByUserid(param);

        }

        result.success = true;
        result.message = "성공";
        return result;
    }

    @PostMapping("/approve")
    @Transactional
    public AjaxResult approve(@RequestParam(value = "userid") String userid,
                              @RequestParam(value = "authgrpcd") String authgrpcd,
                              @RequestParam(value = "authgrpnm") String authgrpnm,
                              Authentication auth)
    {

        List<String> paramList = new UtilClass().parseUserIds(userid);
        List<String> authcdList = new UtilClass().parseUserIds(authgrpcd);
        List<String> authnmList = new UtilClass().parseUserIds(authgrpnm);


        AjaxResult result = new AjaxResult();

        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        OffsetDateTime offsetDateTime = seoulDateTime.toOffsetDateTime();
        Timestamp today = new Timestamp(System.currentTimeMillis());
        try{


            for(int i=0; i < paramList.size(); i++){
                Optional<TB_RP940> TB940list = tb_rp940Repository.findByUserid(paramList.get(i));
                if(TB940list.isPresent()){
                    TB_RP940 tb940 = TB940list.get();
                    User user = createUserFromTB940(tb940, today);

                    userService.SaveUser(user, auth, tb940.getAuthgrpcd(), tb940.getAuthgrpnm());
                    tb_rp940Repository.updateApprflagToYByUserid(paramList.get(i), offsetDateTime, authcdList.get(i), authnmList.get(i));


                }else {
                    result.success = false;
                    result.message = "신청목록에 없습니다.";
                    return result;
                }
            }


                /*for(String param: paramList){

                    Optional<TB_RP940> TB940list = tb_rp940Repository.findByUserid(param);
                    if(TB940list.isPresent()){
                        TB_RP940 tb940 = TB940list.get();
                        User user = createUserFromTB940(tb940, today);

                        userService.SaveUser(user, auth, tb940.getAuthgrpcd(), tb940.getAuthgrpnm());
                        tb_rp940Repository.updateApprflagToYByUserid(param, offsetDateTime, authgrpcd, authgrpnm);


                    }else {
                        result.success = false;
                        result.message = "신청목록에 없습니다.";
                        return result;
                    }

                }*/
            result.success = true;
            result.message = "저장하였습니다.";
            return result;

        }catch(Exception e){
            System.out.println(e);

            result.success = false;
            result.message = "에러가발생하였습니다.";
            return result;
        }
    }



    private User createUserFromTB940(TB_RP940 tb940, Timestamp today) {


        User user = new User();
        user.setPassword(tb940.getLoginpw());
        user.setSuperUser(false); // 이 부분은 상황에 따라 다르게 설정
        user.setUsername(tb940.getUserid());
        user.setFirst_name(tb940.getUsernm());
        user.setEmail(tb940.getUsermail());
        user.setIs_staff(true);
        user.setActive(true);
        user.setDate_joined(today);
        user.setTel(tb940.getUserhp());
        user.setAgencycd(tb940.getAgencycd());
        user.setDivinm(tb940.getDivinm());
        user.setLast_name("");
        return user;
    }


}
