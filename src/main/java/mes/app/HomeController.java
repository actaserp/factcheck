package mes.app;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import mes.app.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import mes.config.Settings;
import mes.domain.entity.SystemOption;
import mes.domain.entity.User;
import mes.domain.repository.SystemOptionRepository;

@Slf4j
@Controller
public class HomeController {
	
	@Autowired
	SystemOptionRepository systemOptionRepository;

	@Autowired
	Settings settings;

	@Autowired
	UserService userService;

	@RequestMapping(value = "/rtsp")
	public String TestPage(){

		return "/rtsp";

	}


	/*@RequestMapping(value= "/", method=RequestMethod.GET)
	public ModelAndView pageIndex(HttpServletRequest request, HttpSession session) {

		SecurityContext sc = SecurityContextHolder.getContext();
		Authentication auth = sc.getAuthentication();
		User user = (User)auth.getPrincipal();
		String userid = user.getUsername();
		String username = user.getUserProfile().getName();;


		SystemOption sysOpt= this.systemOptionRepository.getByCode("LOGO_TITLE");
		String logoTitle = sysOpt.getValue();

		//q = this.systemOptionRepository.getByCode("main_menu");


		ModelAndView mv = new ModelAndView();
		mv.addObject("userid", userid);
		mv.addObject("username", username);
		mv.addObject("userinfo", user);
		mv.addObject("system_title", logoTitle);
//		mv.addObject("default_menu_code", "wm_dashboard_summary");



		mv.setViewName("index");
		return mv;
	}*/
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView pageIndex(HttpServletRequest request, HttpSession session) {

		// User-Agent 확인
		String userAgent = request.getHeader("User-Agent").toLowerCase();
		boolean isMobile = userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone");

		SecurityContext sc = SecurityContextHolder.getContext();
		Authentication auth = sc.getAuthentication();

		// Principal 가져오기
		Object principal = auth.getPrincipal();
		String userid = null;
		String username = null;

		if (principal instanceof User) {
			// Principal이 User 타입일 때 처리
			User user = (User) principal;
			userid = user.getUsername();
			username = user.getUserProfile() != null ? user.getUserProfile().getName() : "Unknown User";
		} else if (principal instanceof String) {
			// Principal이 String 타입일 때 처리 (소셜 로그인 사용 시)
			userid = (String) principal;
			username = "소셜 사용자"; // 적절히 기본 값을 설정
		} else {
			throw new IllegalStateException("Authentication principal is neither User nor String.");
		}

		SystemOption sysOpt = this.systemOptionRepository.getByCode("LOGO_TITLE");
		String logoTitle = sysOpt.getValue();

		ModelAndView mv = new ModelAndView();
		mv.addObject("userid", userid);
		mv.addObject("username", username);
		mv.addObject("system_title", logoTitle);

		// 모바일 첫페이지
		mv.addObject("currentPage", "ticket-list");
		mv.setViewName(isMobile ? "mobile/ticket-list" : "index");
		return mv;
	}


	@RequestMapping(value= "/intro", method=RequestMethod.GET)
    public ModelAndView pageIntro(HttpServletRequest request, HttpSession session) {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("intro");
		return mv;
	}
	

	@RequestMapping(value= "/setup", method=RequestMethod.GET)
	public ModelAndView pageSetup(Authentication auth, HttpServletResponse response) throws IOException {
		
		// 로그아웃된 상태인 경우 로그인페이지로 이동
		if (auth == null) {
		    response.sendRedirect("/login");
			return null;
		} 
		
		User user = (User)auth.getPrincipal();
		String username = user.getUserProfile().getName();
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("username", username);
		mv.addObject("userinfo", user);
		
		mv.setViewName("/system/setup");
		return mv;
	}
	
		
	
}