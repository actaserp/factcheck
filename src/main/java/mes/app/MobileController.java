package mes.app;

import lombok.Getter;
import mes.app.MobileUsr.AppInfo.Notice.NoticeBoardService;
import mes.app.account.service.TB_USERINFOService;
import mes.domain.DTO.NoticeResponseDto;
import mes.domain.entity.User;
import mes.domain.entity.actasEntity.TB_USERINFO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import mes.domain.DTO.FileResponseDto;
import java.util.List;

// 모바일 메뉴 컨트롤러
@Controller
@RequestMapping("/mobile")
public class MobileController {

    private final NoticeBoardService noticeBoardService;
    private final TB_USERINFOService userinfoService;


    public MobileController(NoticeBoardService noticeBoardService, TB_USERINFOService userinfoService) {
        this.noticeBoardService = noticeBoardService;

        this.userinfoService = userinfoService;
    }


    @GetMapping("/ticket-list")
    public String ticketList(Model model) {
        model.addAttribute("currentPage", "ticket-list");
        return "mobile/ticket-list"; // "mobile/ticket-list.html"로 매핑
    }

    @GetMapping("/ticket-register")
    public String ticketRegister(Model model) {
        model.addAttribute("currentPage", "ticket-register");
        return "mobile/ticket-register"; // "mobile/ticket-register.html"로 매핑
    }
    @GetMapping("/kakaoMap2")
    public String kakaoMap2(Model model) {
        model.addAttribute("currentPage", "kakaoMap2");
        return "mobile/kakaoMap2";
    }
    @GetMapping("/mlogin")
    public String mlogin(Model model) {
        model.addAttribute("currentPage", "mlogin");
        return "/mobile/mlogin";
    }

    @GetMapping("/view-list")
    public String viewList(Model model) {
        model.addAttribute("currentPage", "view-list");
        return "mobile/view-list";
    }

    @GetMapping("/IssueInquiry")
    public String IssueInquiry(Model model) {
        model.addAttribute("currentPage", "IssueInquiry");
        return "mobile/IssueInquiry";
    }

    @GetMapping("/IssueInquiryAPI")
    public String IssueInquiryAPI(Model model) {
        model.addAttribute("currentPage", "IssueInquiryAPI");
        return "mobile/IssueInquiryAPI";
    }

    @GetMapping("/UserInfo")
    public String UserInfo(Model model) {
        model.addAttribute("currentPage", "UserInfo");
        return "mobile/UserInfo";
    }

    @GetMapping("/fsr-register")
    public String fsrRegister() {
        return "mobile/fsr-register"; // "mobile/fsr-register.html"로 매핑
    }

    @GetMapping("/fsr-search")
    public String fsrSearch() {
        return "mobile/fsr-search"; // "mobile/fsr-search.html"로 매핑
    }

    @GetMapping("/user-info")
    public String userInfo(Model model) {
        model.addAttribute("currentPage", "user-info");
        return "mobile/user-info"; // "mobile/user-info.html"로 매핑
    }

    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("currentPage", "faq");
        return "mobile/faq"; // "mobile/user-info.html"로 매핑
    }

    @GetMapping("/search_main")
    public String searchMain() {
        return "address_search/search_main"; // "address_search/search_main.html"로 매핑
    }

    @GetMapping("/search_card")
    public String searchCard() {
        return "address_search/search_card"; // "address_search/search_card.html"로 매핑 }
    }

    @GetMapping("/search_sub")
    public String searchSub() {
        return "address_search/search_sub"; // "address_search/search_sub.html"로 매핑 }
    }

    @GetMapping("/version")
    public String versionPage(Model model) {
        model.addAttribute("currentPage", "version");
        return "mobile/appInfo/version"; // "address_search/search_sub.html"로 매핑 }
    }

    @GetMapping("/terms")
    public String TermsPage(Model model) {
        model.addAttribute("currentPage", "terms");
        return "mobile/appInfo/terms"; // "address_search/search_sub.html"로 매핑 }
    }

    @GetMapping("/danger")
    public String DangerPage(Model model) {
        model.addAttribute("currentPage", "danger");
        return "mobile/appInfo/danger"; // "address_search/search_sub.html"로 매핑 }
    }

    @GetMapping("/pie_chart")
    public String piechart() {return "mobile/pie_chart"; }// "mobile/ticket-list.html"로 매핑}

    @GetMapping("/risk")
    public String LiskPage(Model model) {
        model.addAttribute("currentPage", "risk");
        return "mobile/appInfo/risk"; // "address_search/search_sub.html"로 매핑 }
    }

    @GetMapping("/notice")
    public String NoticePage(Model model) {
        model.addAttribute("currentPage", "notice");
        return "mobile/appInfo/notice"; // "address_search/search_sub.html"로 매핑 }
    }


    @GetMapping("/notice/view")
    public String NoticePage(Model modelMap, @RequestParam Integer id) {


        NoticeResponseDto detail = noticeBoardService.NoticeView(id);

        List<FileResponseDto> file = noticeBoardService.NoticeFile(id);

        if(!file.isEmpty()){
            modelMap.addAttribute("fileflag", true);
        }else{
            modelMap.addAttribute("fileflag", false);
        }
        modelMap.addAttribute("currentPage", "notice");
        modelMap.addAttribute("notice", detail);

        return "mobile/appInfo/noticeView";
    }

    @GetMapping("/withdraw")
    public String WithDrawPage(Model model,
                               @AuthenticationPrincipal User user){

        ///TB_USERINFOService

        TB_USERINFO userInfo = userinfoService.getUserInfo(user.getUsername());

        //System.out.println(user);
        TempDto userDto = new TempDto(user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                userInfo.getUserAddr(),
                user.getFirst_name(),
                userInfo.getAgeNum());
        model.addAttribute("currentPage", "withdraw");
        model.addAttribute("user", userDto);

        return "mobile/appInfo/withdraw";

    }


    @Getter
    public class TempDto{
        private String username;
        private String email;
        private String tel;
        private String address;
        private String nickname;
        private Integer age;

        public TempDto(String username, String email, String phone, String adr, String nick, Integer age){
            this.username = username;
            this.email = email;
            this.tel = phone;
            this.address = adr;
            this.nickname = nick;
            this.age = age;
        }

    }

    @GetMapping("/inquiry")
    public String InquiryPage(Model model) {
        model.addAttribute("currentPage", "inquiry");
        return "mobile/appInfo/inquiry"; // "address_search/search_sub.html"로 매핑 }
    }

}