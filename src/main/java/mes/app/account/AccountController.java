package mes.app.account;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.text.html.Option;
import javax.transaction.Transactional;

import mes.app.MailService;
import mes.app.UtilClass;
import mes.app.account.service.TB_RP940_Service;
import mes.app.account.service.TB_RP945_Service;
import mes.domain.DTO.TB_RP940Dto;
import mes.domain.DTO.TB_RP945Dto;
import mes.domain.DTO.UserCodeDto;
import mes.domain.entity.UserCode;
import mes.domain.entity.UserGroup;
import mes.domain.repository.*;
import org.apache.fop.layoutmgr.BorderOrPaddingElement;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RequestParam;

import mes.domain.entity.TB_RP940;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import mes.domain.entity.User;
import mes.domain.model.AjaxResult;
import mes.domain.security.CustomAuthenticationToken;
import mes.domain.security.Pbkdf2Sha256;
import mes.domain.services.AccountService;
import mes.domain.services.SqlRunner;


@RestController
public class AccountController {

	@Autowired
	AccountService accountService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserCodeRepository userCodeRepository;

	@Autowired
	SqlRunner sqlRunner;

	@Autowired
	TB_RP940_Service tbRp940Service;

	@Autowired
	TB_RP945_Service tbRp945Service;


	@Autowired
	TB_RP940Repository tb_rp940Repository;

	@Autowired
	TB_RP945Repository tb_rp945Repository;

	@Autowired
	MailService emailService;


	@Resource(name="authenticationManager")
	private AuthenticationManager authManager;
	@Autowired
	private UserGroupRepository userGroupRepository;



	private final ConcurrentHashMap<String, String> tokenStore = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Long> tokenExpiry = new ConcurrentHashMap<>();

	@GetMapping("/login")
	public ModelAndView loginPage(
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session, Authentication auth) {
		ModelAndView mv = new ModelAndView("login");

		Map<String, Object> userInfo = new HashMap<String, Object>();
		Map<String, Object> gui = new HashMap<String, Object>();

		mv.addObject("userinfo", userInfo);

		mv.addObject("gui", gui);
		if(auth!=null) {
			SecurityContextLogoutHandler handler =  new SecurityContextLogoutHandler();
			handler.logout(request, response, auth);
		}

		return mv;
	}

	@GetMapping("/logout")
	public void logout(
			HttpServletRequest request
			, HttpServletResponse response) throws IOException {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		SecurityContextLogoutHandler handler =  new SecurityContextLogoutHandler();

		this.accountService.saveLoginLog("logout", auth);

		handler.logout(request, response, auth);
		response.sendRedirect("/login");
	}

	@PostMapping("/login")
	public AjaxResult postLogin(
			@RequestParam("username") final String username,
			@RequestParam("password") final String password,
			final HttpServletRequest request) throws UnknownHostException {

		//System.out.println("로그인 데이터: " + username + " / " + password);

		AjaxResult result = new AjaxResult();

		HashMap<String, Object> data = new HashMap<String, Object>();
		result.data = data;

		UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(username, password);
		CustomAuthenticationToken auth = null;
		try{
			auth = (CustomAuthenticationToken)authManager.authenticate(authReq);
		}catch (AuthenticationException e){
			//e.printStackTrace();
			data.put("code", "NOUSER");
			return result;
		}

		if(auth!=null) {
			User user = (User)auth.getPrincipal();
			user.getActive();
			data.put("code", "OK");

			try {
				this.accountService.saveLoginLog("login", auth);
			} catch (UnknownHostException e) {
				// Handle the exception (e.g., log it)
				e.printStackTrace();
			}
		} else {
			result.success=false;
			data.put("code", "NOID");
		}

		SecurityContext sc = SecurityContextHolder.getContext();
		sc.setAuthentication(auth);

		HttpSession session = request.getSession(true);
		session.setAttribute("SPRING_SECURITY_CONTEXT", sc);

		return result;
	}

	@GetMapping("/account/myinfo")
	public AjaxResult getUserInfo(Authentication auth){
		User user = (User)auth.getPrincipal();
		AjaxResult result = new AjaxResult();

		Map<String, Object> dicData = new HashMap<String, Object>();
		dicData.put("login_id", user.getUsername());
		dicData.put("name", user.getUserProfile().getName());
		result.data = dicData;
		return result;
	}

	@PostMapping("/account/myinfo/password_change")
	public AjaxResult userPasswordChange(
			@RequestParam("name") final String name,
			@RequestParam("loginPwd") final String loginPwd,
			@RequestParam("loginPwd2") final String loginPwd2,
			Authentication auth
	) {

		User user = (User)auth.getPrincipal();
		AjaxResult result = new AjaxResult();

		if (StringUtils.hasText(loginPwd)==false | StringUtils.hasText(loginPwd2)==false) {
			result.success=false;
			result.message="The verification password is incorrect.";
			return result;
		}

		if(loginPwd.equals(loginPwd2)==false) {
			result.success=false;
			result.message="The verification password is incorrect.";
			return result;
		}

		user.setPassword(Pbkdf2Sha256.encode(loginPwd2));
		//user.getUserProfile().setName(name);
		this.userRepository.save(user);

		String sql = """
        	update user_profile set 
        	"Name"=:name, _modified = now(), _modifier_id=:id 
        	where id=:id 
        """;

		MapSqlParameterSource dicParam = new MapSqlParameterSource();
		dicParam.addValue("name", name);
		dicParam.addValue("id", user.getId());
		this.sqlRunner.execute(sql, dicParam);


		return result;
	}


	/***
	 *  아이디 중복 확인
	 * **/
	@PostMapping("/useridchk")
	public AjaxResult IdChk(@RequestParam("userid") final String userid){

		AjaxResult result = new AjaxResult();


		Optional<TB_RP940> rp940 =  tb_rp940Repository.findByUserid(userid);
		Optional<User> user = userRepository.findByUsername(userid);



		if(rp940.isPresent()){
			result.success = false;
			result.message = "이미 신청 완료하였습니다.";
			return result;

		}
		if(!user.isPresent()){

			result.success = true;
			result.message = "사용할 수 있는 계정입니다.";
			return result;

		}else {
			result.success = false;
			result.message = "중복된 계정이 존재합니다.";
			return result;
		}


	}

	/**권한신청**/
	@PostMapping("/Register/save")
	@Transactional
	public AjaxResult RegisterUser(
			@RequestParam(value = "agency") String agency,
			@RequestParam(value = "agencyDepartment") String agencyDepartment,
			@RequestParam(value = "level", required = false) String level,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "tel", required = false) String tel,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "id") String id,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "authType") String authType,
			@RequestParam(value = "spworkcd") String spworkcd,
			@RequestParam(value = "spcompcd") String spcompcd,
			@RequestParam(value = "spplancd") String spplancd,
			@RequestParam(value = "reason") String reason,
			@RequestParam(value = "firstText") String firstText,
			@RequestParam(value = "secondText") String secondText,
			@RequestParam(value = "thirdText") String thirdText,
			@RequestParam(value = "authTypeText") String authTypeText,
			@RequestParam(value = "agencynm") String agencynm,
			@RequestParam(value = "AuthenticationCode") String AuthenticationCode

	){

		AjaxResult result = new AjaxResult();

		try {
			result = verifyAuthenticationCode(AuthenticationCode, email);
			if(result.success){
				//클라에서 동적으로 값이 넘어와서 몇개인지 모름, 그래서 쉼표구분자로 리스트형태로 분개해서 서버에서 노가다 뛰어여한다.


				UtilClass util = new UtilClass();
				List<Integer> spworkidList = util.parseUserIdsToInt(spworkcd);
				List<Integer> spcompidList = util.parseUserIdsToInt(spcompcd);
				List<Integer> spplanidList = util.parseUserIdsToInt(spplancd);


				List<String> firstTextList = Arrays.asList(firstText.split(","));
				List<String> secondTextList = Arrays.asList(secondText.split(","));
				List<String> thirdTextList = Arrays.asList(thirdText.split(","));


				TB_RP940Dto dto = TB_RP940Dto.builder()
						.agency(agency)
						.agencynm(agencynm)
						.agencyDepartment(agencyDepartment)
						.authType(authType)
						.authgrpnm(authTypeText)
						.appflag("N")
						.email(email)
						.id(id)
						.level(level)
						.tel(tel.replaceAll("-",""))
						.name(name)
						.password(Pbkdf2Sha256.encode(password))
						.reason(reason)
						.build();

				tbRp940Service.save(dto);

				//신청순번의 최대값을 구한후 +1을 하고 문자열로 바꿔줌
				//이거 반복문 안에 넣으면 db호출이 너무 많다.
				String RawAskSeq = tb_rp945Repository.findMaxAskSeq();
				RawAskSeq = (RawAskSeq != null) ? RawAskSeq : "0";

				int AskSeqInt = Integer.parseInt(RawAskSeq) + 1;


				// 필요한 모든 UserCode를 한 번에 조회
				Map<Integer, UserCode> spworkCodes = userCodeRepository.findAllById(spworkidList)
						.stream().collect(Collectors.toMap(UserCode::getId, Function.identity()));
				Map<Integer, UserCode> spcompCodes = userCodeRepository.findAllById(spcompidList)
						.stream().collect(Collectors.toMap(UserCode::getId, Function.identity()));
				Map<Integer, UserCode> spplanCodes = userCodeRepository.findAllById(spplanidList)
						.stream().collect(Collectors.toMap(UserCode::getId, Function.identity()));

				for(int i=0; i<spworkidList.size(); i++){


					String askseq = String.format("%03d", AskSeqInt);

					UserCode spworkid = spworkCodes.get(spworkidList.get(i));
					UserCode spcompid = spcompCodes.get(spcompidList.get(i));
					UserCode spplanid = spplanCodes.get(spplanidList.get(i));


					TB_RP945Dto dto2 = TB_RP945Dto.builder()
							.userid(id)
							.askseq(askseq)
							.spworkcd(spworkid.getCode())
							.spcompcd(spcompid.getCode())
							.spplancd(spplanid.getCode())
							.spworknm(firstTextList.get(i))
							.spcompnm(secondTextList.get(i))
							.spplannm(thirdTextList.get(i))
							.spworkid(spworkid.getId())
							.spcompid(spcompid.getId())
							.spplanid(spplanid.getId())
							.build();

					tbRp945Service.save(dto2);

					AskSeqInt++;
				}

				result.success = true;
				result.message = "신청이 완료되었습니다.";
				return result;
			}else{
				return result;
			}
		} catch(Exception e){
			System.out.println(e);

			result.success = false;
			result.message = "에러가발생하였습니다.";
			return result;
		}

	}

	@PostMapping("/user-auth/searchAccount")
	public AjaxResult IdSearch(@RequestParam("usernm") final String usernm,
							   @RequestParam("mail") final String mail){

		AjaxResult result = new AjaxResult();

		List<String> user = userRepository.findByFirstNameAndEmailNative(usernm, mail);

		if(!user.isEmpty()){
			result.success = true;
			result.data = user;
		}else {
			result.success = false;
			result.message = "해당 사용자가 존재하지 않습니다.";
		}
		return result;
	}

	@PostMapping("/user-auth/AuthenticationEmail")
	public AjaxResult PwSearch(@RequestParam("usernm") final String usernm,
							   @RequestParam("mail") final String mail){

		AjaxResult result = new AjaxResult();

		if(usernm.equals("empty")){
			sendEmailLogic(mail, "신규사용자");

			result.success = true;
			result.message = "인증 메일이 발송되었습니다.";
			return result;
		}

		boolean flag = userRepository.existsByUsernameAndEmail(usernm, mail);

		if(flag) {
			sendEmailLogic(mail, usernm);

			result.success = true;
			result.message = "인증 메일이 발송되었습니다.";
		}else {
			result.success = false;
			result.message = "해당 사용자가 존재하지 않습니다.";
		}

		return result;
	}

	private void sendEmailLogic(String mail, String usernm){
		Random random = new Random();
		int randomNum = 100000 + random.nextInt(900000); // 100000부터 999999까지의 랜덤 난수 생성
		String verificationCode = String.valueOf(randomNum); // 정수를 문자열로 변환
		emailService.sendVerificationEmail(mail, usernm, verificationCode);

		tokenStore.put(mail, verificationCode);
		tokenExpiry.put(mail, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3));

	}

	@PostMapping("/user-auth/verifyCode")
	public AjaxResult verifyCode(@RequestParam("code") final String code,
								 @RequestParam("mail") final String mail,
								 @RequestParam("password") final String password,
								 @RequestParam("userid") final String userid
	){

		AjaxResult result = new AjaxResult();
		result = verifyAuthenticationCode(code, mail);

		if(result.success){
			String pw = Pbkdf2Sha256.encode(password);


			userRepository.PasswordChange(pw, userid);


			result.success = true;
			result.message = "비밀번호가 변경되었습니다.";

			return result;
		}else{
			return result;
		}

		/*AjaxResult result = new AjaxResult();


		String storedToken = tokenStore.get(mail);

		if(storedToken != null && storedToken.equals(code)){
			long expiryTime = tokenExpiry.getOrDefault(mail, 0L);
			if(System.currentTimeMillis() > expiryTime){
				result.success = false;
				result.message = "인증 코드가 만료되었습니다.";
				tokenStore.remove(mail);
				tokenExpiry.remove(mail);
			} else {

				String pw = Pbkdf2Sha256.encode(password);


				userRepository.PasswordChange(pw, userid);


				result.success = true;
				result.message = "비밀번호가 변경되었습니다.";
			}
		}else {
			result.success = false;
			result.message = "인증 코드가 유효하지 않습니다.";
		}*/


	}

	private AjaxResult verifyAuthenticationCode(String code, String mail){

		AjaxResult result = new AjaxResult();

		String storedToken = tokenStore.get(mail);
		if(storedToken != null && storedToken.equals(code)){
			long expiryTime = tokenExpiry.getOrDefault(mail, 0L);
			if(System.currentTimeMillis() > expiryTime){
				result.success = false;
				result.message = "인증 코드가 만료되었습니다.";
				tokenStore.remove(mail);
				tokenExpiry.remove(mail);
			} else {
				result.success = true;
				result.message = "비밀번호가 변경되었습니다.";
			}
		}else{
			result.success = false;
			result.message = "인증 코드가 유효하지 않습니다.";
		}
		return result;
	}


	@GetMapping("/user-codes/parent")
	public List<UserCodeDto> getUserCodeByParentId(@RequestParam Integer parentId){

		List<UserCode> list = userCodeRepository.findByParentId(parentId);

		List<UserCodeDto> dtoList = list.stream()
				.map(userCode -> new UserCodeDto(
						userCode.getId(),
						userCode.getCode(),
						userCode.getValue()
				))
				.toList();

		return dtoList;
	}

	@GetMapping("/user-auth/type")
	public List<UserGroup> getUserAuthTypeAll(){
		return userGroupRepository.findAll();

	}

	@GetMapping("/user-codes/code")
	public List<UserCodeDto> getUserCodesByCode(@RequestParam Integer code) {

		List<UserCode> list = userCodeRepository.findByParentId(code);

		List<UserCodeDto> dtoList = list.stream()
				.map(userCode -> new UserCodeDto(
						userCode.getId(),
						userCode.getCode(),
						userCode.getValue()
				)).toList();

		return dtoList;
	}



}