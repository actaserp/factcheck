package mes.app.account;

import lombok.extern.slf4j.Slf4j;
import mes.app.MailService;
import mes.app.account.service.TB_USERINFOService;
import mes.app.account.service.TB_xuserService;
import mes.app.system.service.UserService;
import mes.app.user.enums.SocialProvider;
import mes.domain.DTO.UserCodeDto;
import mes.domain.entity.User;
import mes.domain.entity.UserCode;
import mes.domain.entity.UserGroup;
import mes.domain.entity.actasEntity.TB_USERINFO;
import mes.domain.entity.actasEntity.TB_XUSERS;
import mes.domain.entity.actasEntity.TB_XUSERSId;
import mes.domain.model.AjaxResult;
import mes.domain.repository.UserCodeRepository;
import mes.domain.repository.UserGroupRepository;
import mes.domain.repository.UserRepository;
import mes.domain.repository.actasRepository.TB_USERINFORepository;
import mes.domain.repository.actasRepository.TB_XuserRepository;
import mes.domain.security.CustomAuthenticationToken;
import mes.domain.security.Pbkdf2Sha256;
import mes.domain.services.AccountService;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
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
  MailService emailService;

  @Autowired
  TB_xuserService XusersService;

  @Autowired
  TB_USERINFOService userInfoService;

  @Autowired
  private UserService userService;

  @Resource(name = "authenticationManager")
  private AuthenticationManager authManager;
  @Autowired
  private UserGroupRepository userGroupRepository;
  @Autowired
  JdbcTemplate jdbcTemplate;

  @Autowired
  TB_USERINFORepository tb_userinfoRepository;

  private Boolean flag;
  private Boolean flag_pw;

  private final ConcurrentHashMap<String, String> tokenStore = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Long> tokenExpiry = new ConcurrentHashMap<>();
  @Autowired
  private TB_XuserRepository tB_XuserRepository;

 @GetMapping("/login")
 public ModelAndView loginPage(
     HttpServletRequest request,
     HttpServletResponse response,
     HttpSession session, Authentication auth) {

   //User-Agent를 기반으로 모바일 여부 감지
   String userAgent = request.getHeader("User-Agent").toLowerCase();
   boolean isMobile = userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone");

   // 세션을 이용해 모바일에서 한 번만 리디렉션되도록 설정
   Boolean isMobileRedirected = (Boolean) session.getAttribute("isMobileRedirected");

   if (isMobile && (isMobileRedirected == null || !isMobileRedirected)) {
     session.setAttribute("isMobileRedirected", true);  // ✅ 모바일에서 리디렉션 상태 저장
     return new ModelAndView("redirect:/MobileFirstPage");
   }

   // 모바일이면 "mlogin" 뷰 반환, 웹이면 "login" 뷰 반환
   ModelAndView mv = new ModelAndView(isMobile ? "mlogin" : "login");

   // 기존의 userinfo 및 gui 데이터를 추가
   Map<String, Object> userInfo = new HashMap<>();
   Map<String, Object> gui = new HashMap<>();

   mv.addObject("userinfo", userInfo);
   mv.addObject("gui", gui);

   // 로그인되어 있는 경우 로그아웃 처리
   if (auth != null) {
     SecurityContextLogoutHandler handler = new SecurityContextLogoutHandler();
     handler.logout(request, response, auth);
   }

   return mv;
 }

  @GetMapping("/MobileFirstPage")
  public ModelAndView mobileFirstPage(HttpSession session) {
    session.removeAttribute("isMobileRedirected");  // ✅ 모바일 첫 페이지에서 세션 값 초기화
    return new ModelAndView("/mobile/MobileFirstPage");
  }

  @GetMapping("/logout")
  public void logout(
      HttpServletRequest request
      , HttpServletResponse response) throws IOException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    SecurityContextLogoutHandler handler = new SecurityContextLogoutHandler();

    this.accountService.saveLoginLog("logout", auth);

    handler.logout(request, response, auth);
    response.sendRedirect("/login");
  }

  @PostMapping("/login")
  public AjaxResult postLogin(
      @RequestParam("username") final String username,
      @RequestParam(value = "password", required = false) final String password,
      @RequestParam(value = "socialProvider", required = false) final String socialProvider,
      final HttpServletRequest request) throws UnknownHostException {

    AjaxResult result = new AjaxResult();
    HashMap<String, Object> data = new HashMap<>();
    result.data = data;

    // 사용자 조회
    Optional<User> optionalUser = userRepository.findIsActiveUserByUsername(username);
    if (optionalUser.isEmpty()) {
      data.put("code", "NOUSER");
      return result;
    }

    User user = optionalUser.get();

    // 소셜 로그인 처리
    if (password == null || password.isEmpty()) {
      // 소셜 로그인 로직 추가
      if (socialProvider == null || socialProvider.isEmpty()) {
        data.put("code", "MISSING_SOCIAL_PROVIDER");
        return result;
      }

      // 소셜 제공자 검증
      try {
        SocialProvider provider = SocialProvider.valueOf(socialProvider.toUpperCase());
        //log.info("유효한 소셜 제공자: {}", provider);

        // 인증 처리
        authenticateUser(user, request);

        // 소셜 로그인 성공 응답
        data.put("code", "OK");
        data.put("socialProvider", provider.name());
        return result;

      } catch (IllegalArgumentException e) {
        data.put("code", "INVALID_SOCIAL_PROVIDER");
        return result;
      }
    }

    // 일반 로그인 처리
    UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(username, password);
    CustomAuthenticationToken auth = (CustomAuthenticationToken) authManager.authenticate(authReq);

    if (auth != null) {
      data.put("code", "OK");

      try {
        accountService.saveLoginLog("login", auth);
      } catch (UnknownHostException e) {
        log.error("로그 저장 중 에러 발생", e);
      }

      // Spring Security 세션 설정
      authenticateUser(auth.getPrincipal(), request);
    } else {
      result.success = false;
      data.put("code", "NOID");
    }

    return result;
  }

  @GetMapping("/check-login")
  @ResponseBody
  public String checkLogin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.isAuthenticated()
        && !"anonymousUser".equals(authentication.getPrincipal())) {
      return "로그인된 사용자: " + authentication.getName();
    } else {
      return "로그인이 필요합니다.";
    }
  }

  /**
   * Spring Security 인증 객체를 생성하고 세션에 저장하는 메서드
   */
  private void authenticateUser(Object principal, HttpServletRequest request) {
    UsernamePasswordAuthenticationToken authReq =
        new UsernamePasswordAuthenticationToken(principal, null, List.of());
    SecurityContext sc = SecurityContextHolder.getContext();
    sc.setAuthentication(authReq);

    HttpSession session = request.getSession(true);
    session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
  }


  @GetMapping("/account/myinfo")
  public AjaxResult getUserInfo(Authentication auth) {
    User user = (User) auth.getPrincipal();
    AjaxResult result = new AjaxResult();

    Map<String, Object> dicData = new HashMap<String, Object>();
    dicData.put("id", user.getId());
    dicData.put("login_id", user.getUsername());
    dicData.put("name", user.getUserProfile().getName());
    dicData.put("email", user.getEmail());
    dicData.put("phone", user.getPhone());
    result.data = dicData;
    return result;
  }

  @GetMapping("/mobile/myinfo")
  public AjaxResult getMobileUserInfo(Authentication auth) {
    AjaxResult result = new AjaxResult();
    User user = (User) auth.getPrincipal();
    TB_USERINFO userInfo = userInfoService.getUserInfo(user.getUsername());
    result.data =userInfo;
    return result;
  }

  @PostMapping("/mobile/userInfo/update")
  @Transactional
  public AjaxResult mobileUpdateInfo(@RequestBody Map<String, Object> requestData, Authentication auth) {
    AjaxResult result = new AjaxResult();

    try {
      // 현재 로그인한 사용자 ID 가져오기
      User user = (User) auth.getPrincipal();
      String userId = user.getUsername();

      // 사용자 정보 조회
      TB_USERINFO existingUser = tb_userinfoRepository.findById(userId)
              .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));

      User existingAuthUser = userRepository.findIsActiveUserByUsername(userId)
              .orElseThrow(() -> new EntityNotFoundException("사용자 계정 정보를 찾을 수 없습니다."));

      // 요청 데이터에서 수정할 값 가져오기
      String userNm = (String) requestData.get("userNm");
      String birthYear = (String) requestData.get("birthYear");
      String sexYn = (String) requestData.get("sexYn");
      String postCd = (String) requestData.get("postCd");
      String userAddr = (String) requestData.get("userAddr");
      String userHp = (String) requestData.get("userHp");
      String userMail = (String) requestData.get("userMail");
      String loginPw = (String) requestData.get("loginPw");

      int ageNum = 0;
      if (birthYear != null && !birthYear.isEmpty()) {
        try {
          LocalDate birthDate;
          // 먼저 yyyy-MM-DD 형식으로 파싱 시도
          if (birthYear.length() == 10) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            birthDate = LocalDate.parse(birthYear, formatter);
          }
          // yyyy-MM 형식으로 파싱 시도
          else if (birthYear.length() == 7) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            birthDate = LocalDate.parse(birthYear, formatter);
          } else {
            throw new IllegalArgumentException("유효하지 않은 birthYear 형식: " + birthYear);
          }

          // 나이 계산
          LocalDate currentDate = LocalDate.now();
          ageNum = Period.between(birthDate, currentDate).getYears();
          System.out.println(ageNum);
        } catch (Exception e) {
          log.error("생년월일 처리 중 오류 발생 - birthYear: {}", birthYear, e);
          throw e;
        }
      }
      if (birthYear != null && !birthYear.isEmpty()) {
        try {
          // yyyy-MM-dd 형식을 LocalDate로 변환
          DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
          LocalDate birthDate = LocalDate.parse(birthYear, inputFormatter);

          // yyMMdd 형식으로 변환
          DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyMMdd");
          birthYear = birthDate.format(outputFormatter);

        } catch (Exception e) {
          log.error("birthYear 가공 중 오류 발생 - 입력값: {}", birthYear, e);
          throw e; // 필요한 경우 예외 처리
        }
      }
      if (userAddr != null && !userAddr.isEmpty()) {
        // 주소를 파싱하여 시도와 구군 추출
        Map<String, String> addressParts = parseAddress(userAddr);
        String userIDO = addressParts.getOrDefault("userIDO", ""); // 시도
        String userGU = addressParts.getOrDefault("userGU", "");   // 구군
      }

      // 주소에서 시도와 구군 추출
      String userIDO = "";
      String userGU = "";
      if (userAddr != null && !userAddr.isEmpty()) {
        Map<String, String> addressParts = parseAddress(userAddr);
        userIDO = addressParts.getOrDefault("userIDO", ""); // 시도
        userGU = addressParts.getOrDefault("userGU", "");   // 구군
      }

      // 필드 업데이트 (필수 값)
      existingUser.setUserNm(userNm);
      existingUser.setBirthYear(birthYear);
      existingUser.setSexYn(sexYn);
      existingUser.setPostCd(postCd);
      existingUser.setUserAddr(userAddr);
      existingUser.setUserHp(userHp);
      existingUser.setUserMail(userMail);
      existingUser.setAgeNum(ageNum);
      existingUser.setUserIDO(userIDO);
      existingUser.setUserGU(userGU);
      existingUser.setInDatem(LocalDateTime.now());

      System.out.println("existingUser"+existingUser);
      System.out.println("existingAuthUser"+existingAuthUser);
      // 사용자 정보 저장
      tb_userinfoRepository.save(existingUser);

      // 비밀번호 저장
      existingAuthUser.setPassword(Pbkdf2Sha256.encode(loginPw));
      userRepository.save(existingAuthUser);

      result.success = true;
      result.message = "사용자 정보가 성공적으로 업데이트되었습니다.";
    } catch (Exception e) {
      result.success = false;
      result.message = "저장 중 오류가 발생했습니다: " + e.getMessage();
    }

    return result;
  }


  @PostMapping("/account/userInfo/update")
  @Transactional
  public AjaxResult updateInfo(@RequestBody Map<String, Object> requestData, Authentication auth) {
    AjaxResult result = new AjaxResult();

    try {
      // 요청 데이터 로그
      log.info("Received Request Data: {}", requestData);

      // 데이터 매핑
      Integer id = (requestData.get("id") != null && !requestData.get("id").toString().isEmpty())
          ? Integer.valueOf(requestData.get("id").toString())
          : null; // ID는 Integer로 변환
      String loginId = (String) requestData.get("login_id");
      String name = (String) requestData.get("name");
      String email = (String) requestData.get("email");
      String password = (String) requestData.get("password");
      String phone = (requestData.get("phone") != null)
          ? (String) requestData.get("phone")
          : ""; // "phone" 키가 없을 경우 기본값 빈 문자열 설정
      Integer userGroupId = (requestData.get("UserGroup_id") != null && !requestData.get("UserGroup_id").toString().isEmpty())
          ? Integer.valueOf(requestData.get("UserGroup_id").toString())
          : 0; // 기본값 0 설정

      Boolean isActive = true; // 기본값
      if (requestData.get("is_active") != null) {
        Object isActiveValue = requestData.get("is_active");
        if (isActiveValue instanceof Number) {
          isActive = ((Number) isActiveValue).intValue() == 1;
        } else if (isActiveValue instanceof String) {
          isActive = "1".equals(isActiveValue) || "true".equalsIgnoreCase(isActiveValue.toString());
        }
      }


      // "ZZ" 값을 전달하여 호출
      List<Map<String, Object>> results = userService.getCustcdAndSpjangcd("ZZ");

      if (results.isEmpty()) {
      } else {
        results.forEach(row -> {
        });
      }
      // 첫 번째 조회된 데이터 사용
      Map<String, Object> firstRow = results.get(0);
      String custcd = (String) firstRow.get("custcd");
      String spjangcd = (String) firstRow.get("spjangcd");

      User user;
      if (id != null && userService.existsById(id)) {
        // 기존 사용자 업데이트
        user = userService.findById(id).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setUsername(loginId);
        user.setPassword(Pbkdf2Sha256.encode(password));
        user.setPhone(phone);
        user.setEmail(email);
        user.setFirst_name(name);
        user.setLast_name(name);
        user.setSpjangcd(spjangcd);
        user.setActive(isActive);
        user.setDate_joined(user.getDate_joined()); // 기존 가입 날짜 유지
      } else {
        // 신규 사용자 생성
        user = User.builder()
            .username(loginId)
            .password(Pbkdf2Sha256.encode(password))
            .phone(phone)
            .email(email)
            .first_name(name)
            .last_name(name)
            .tel("")
            .spjangcd(spjangcd)
            .active(isActive)
            .is_staff(false)
            .date_joined(new Timestamp(System.currentTimeMillis()))
            .superUser(false)
            .build();
      }

      userService.save(user);

      // 사용자 프로필 처리
      String sql;
      int rowsUpdated;

      // 먼저 UPDATE를 시도
      jdbcTemplate.execute("SET IDENTITY_INSERT user_profile ON");
      sql = """
          	UPDATE user_profile
          	SET Name = ?
          	WHERE User_id = ?
          """;
      rowsUpdated = jdbcTemplate.update(sql,
          name,             // Name (사용자 이름)
          user.getId()      // User_id (사용자 ID)
      );
      jdbcTemplate.execute("SET IDENTITY_INSERT user_profile OFF");
      // UPDATE가 적용되지 않은 경우 INSERT 실행
      if (rowsUpdated == 0) {
        jdbcTemplate.execute("SET IDENTITY_INSERT user_profile ON");
        sql = """
            	INSERT INTO user_profile (_created, lang_code, Name, UserGroup_id, User_id)
            	VALUES (?, ?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql,
            new Timestamp(System.currentTimeMillis()), // 현재 시간
            "ko-KR",                                   // lang_code (언어 코드)
            name,                                      // Name (사용자 이름)
            userGroupId,                               // UserGroup_id (사용자 그룹 ID)
            user.getId()                               // User_id (사용자 ID)
        );
        jdbcTemplate.execute("SET IDENTITY_INSERT user_profile OFF");
      }


      //log.info("User Profile 저장 또는 업데이트 완료");

      // XUSERS 정보 설정 및 저장
      TB_XUSERS xusers = TB_XUSERS.builder()
//          .passwd1(password != null ? password : user.getPassword())
//          .passwd2(password != null ? password : user.getPassword())
          .shapass(Pbkdf2Sha256.encode(password != null ? password : user.getPassword()))
          .pernm(name)
          .useyn("1")
          .domcls("%")
          .spjangcd(spjangcd)
          .id(new TB_XUSERSId(custcd, loginId))
          .build();
      XusersService.save(xusers);
      //log.info("Xusers entry saved successfully for loginId {}", loginId);

      result.success = true;
      result.message = "저장되었습니다.";
      result.data = user;

    } catch (Exception e) {
      //log.error("Error occurred during saveOrUpdate: {}", e.getMessage(), e);
      result.success = false;
      result.message = "저장 중 오류가 발생했습니다: " + e.getMessage();
    }

    return result;
  }

  @PostMapping("/account/myinfo/password_change")
  public AjaxResult userPasswordChange(
      @RequestParam("name") final String name,
      @RequestParam("loginPwd") final String loginPwd,
      @RequestParam("loginPwd2") final String loginPwd2,
      Authentication auth
  ) {

    User user = (User) auth.getPrincipal();
    AjaxResult result = new AjaxResult();

    if (StringUtils.hasText(loginPwd) == false | StringUtils.hasText(loginPwd2) == false) {
      result.success = false;
      result.message = "The verification password is incorrect.";
      return result;
    }

    if (loginPwd.equals(loginPwd2) == false) {
      result.success = false;
      result.message = "The verification password is incorrect.";
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
  public AjaxResult IdChk(@RequestParam("userid") final String userid) {

    AjaxResult result = new AjaxResult();

    Optional<User> user = userRepository.findIsActiveUserByUsername(userid);

    if (!user.isPresent()) {

      result.success = true;
      result.message = "사용할 수 있는 계정입니다.";
      return result;

    } else {
      result.success = false;
      result.message = "중복된 계정이 존재합니다.";
      return result;
    }


  }

  /**
   * 사용자등록(관리자)
   **/
  @PostMapping("/Register/save")
  @Transactional
  public AjaxResult RegisterUser(
      @RequestParam(value = "saveName") String name,
      @RequestParam(value = "userHP", required = false) String userHP,
      @RequestParam(value = "email", required = false) String email,
      @RequestParam(value = "id") String id,
      @RequestParam(value = "password") String password,
      @RequestParam(value = "AuthenticationCode") String AuthenticationCode

  ) {
    AjaxResult result = new AjaxResult();

    try {
      result = verifyAuthenticationCode(AuthenticationCode, email);
      if (result.success) {
        // "ZZ" 값을 전달하여 호출
        List<Map<String, Object>> results = userService.getCustcdAndSpjangcd("ZZ");

        if (results.isEmpty()) {
          System.out.println("No data found for spjangcd = 'ZZ'");
        } else {
          results.forEach(row -> {
            System.out.println("custcd: " + row.get("custcd"));
            System.out.println("spjangcd: " + row.get("spjangcd"));
          });
        }
        // 첫 번째 조회된 데이터 사용
        Map<String, Object> firstRow = results.get(0);
        String custcd = (String) firstRow.get("custcd");
        String spjangcd = (String) firstRow.get("spjangcd");
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        User user = User.builder()  //auth_user
            .username(id)
            .password(Pbkdf2Sha256.encode(password))
            .phone(userHP)
            .email(email)
            .first_name(name)
            .last_name(name)
            .tel("")
            .spjangcd(spjangcd)
            .active(true)
            .is_staff(false)
            .date_joined(new Timestamp(System.currentTimeMillis()))
            .superUser(false)
            .build();

        userService.save(user);

        jdbcTemplate.execute("SET IDENTITY_INSERT user_profile ON");
        // UserProfile 저장 (JDBC 사용)
        String sql = "INSERT INTO user_profile (_created, lang_code, Name, UserGroup_id, User_id) VALUES (?,?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            new Timestamp(System.currentTimeMillis()), // 현재 시간
            "ko-KR", // lang_code (예: 한국어)
            name, // Name (사용자 이름)
            14,// marketing_manager (마케팅 관리자)
            user.getId() // User_id
        );
        jdbcTemplate.execute("SET IDENTITY_INSERT user_profile OFF");

        TB_XUSERS xusers = TB_XUSERS.builder()
//            .passwd1(password)
//            .passwd2(password)
            .shapass(Pbkdf2Sha256.encode(password))
            .pernm(name)
            .useyn("1")
            .domcls("%")
            .spjangcd(spjangcd)
            .id(new TB_XUSERSId(custcd, id))
            .build();

        XusersService.save(xusers);

        result.success = true;
        result.message = "등록이 완료되었습니다.";
        return result;
      } else {
        return result;
      }
    } catch (Exception e) {
      System.out.println(e);

      result.success = false;
      result.message = "에러가발생하였습니다.";
      return result;
    }

  }

  /**
   * 사용자등록(모바일)
   **/
  @PostMapping("/mobile/save")
  @Transactional
  public AjaxResult mobileUser(
      @RequestParam(value = "id") String id,
      @RequestParam(value = "password") String password,
      @RequestParam(value = "name") String name, // 필수
      @RequestParam(value = "userHP") String userHP, // 필수
      @RequestParam(value = "email") String email, // 필수
      @RequestParam(value = "birthYear") String birthYear, // 필수
      @RequestParam(value = "sexYn") String sexYn, // 필수
      @RequestParam(value = "postno") String postno, // 필수
      @RequestParam(value = "address1") String address, // 필수
      @RequestParam(value = "AuthenticationCode") String AuthenticationCode // 필수
  ) {
    AjaxResult result = new AjaxResult();
//		log.info("모바일 사용자 등록 요청 시작");
//		log.info("받은 데이터 - id: {}, name: {}, email: {}, birthYear: {}, sexYn: {}, postno: {}",id, name, email, birthYear, sexYn, postno);
    try {
      result = verifyAuthenticationCode(AuthenticationCode, email);
      if (result.success) {
        // "ZZ" 값을 전달하여 호출
        List<Map<String, Object>> results = userService.getCustcdAndSpjangcd("ZZ");

        // 첫 번째 조회된 데이터 사용
        Map<String, Object> firstRow = results.get(0);
        String custcd = (String) firstRow.get("custcd");
        String spjangcd = (String) firstRow.get("spjangcd");

        User user = User.builder()  //auth_user
            .username(id)
            .password(Pbkdf2Sha256.encode(password))
            .phone(userHP)
            .email(email)
            .first_name(name)
            .last_name(name)
            .tel("")
            .spjangcd(spjangcd)
            .active(true)
            .is_staff(false)
            .date_joined(new Timestamp(System.currentTimeMillis()))
            .superUser(false)
            .build();

        userService.save(user);

        jdbcTemplate.execute("SET IDENTITY_INSERT user_profile ON");
        // UserProfile 저장 (JDBC 사용)
        String sql = "INSERT INTO user_profile (_created, lang_code, Name, UserGroup_id, User_id) VALUES (?,?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            new Timestamp(System.currentTimeMillis()), // 현재 시간
            "ko-KR", // lang_code (예: 한국어)
            name, // Name (사용자 이름)
            36,// marketing_manager (마케팅 관리자)
            user.getId() // User_id
        );
        jdbcTemplate.execute("SET IDENTITY_INSERT user_profile OFF");

        int ageNum = 0;
        if (birthYear != null && !birthYear.isEmpty()) {
          try {
            LocalDate birthDate;
            // 먼저 yyyy-MM-DD 형식으로 파싱 시도
            if (birthYear.length() == 10) {
              DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
              birthDate = LocalDate.parse(birthYear, formatter);
            }
            // yyyy-MM 형식으로 파싱 시도
            else if (birthYear.length() == 7) {
              DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
              birthDate = LocalDate.parse(birthYear, formatter);
            } else {
              throw new IllegalArgumentException("유효하지 않은 birthYear 형식: " + birthYear);
            }

            // 나이 계산
            LocalDate currentDate = LocalDate.now();
            ageNum = Period.between(birthDate, currentDate).getYears();
          } catch (Exception e) {
            log.error("생년월일 처리 중 오류 발생 - birthYear: {}", birthYear, e);
            throw e;
          }
        }
        if (birthYear != null && !birthYear.isEmpty()) {
          try {
            // yyyy-MM-dd 형식을 LocalDate로 변환
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate birthDate = LocalDate.parse(birthYear, inputFormatter);

            // yyMMdd 형식으로 변환
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyMMdd");
            birthYear = birthDate.format(outputFormatter);

          } catch (Exception e) {
            log.error("birthYear 가공 중 오류 발생 - 입력값: {}", birthYear, e);
            throw e; // 필요한 경우 예외 처리
          }
        }
        if (address != null && !address.isEmpty()) {
          // 주소를 파싱하여 시도와 구군 추출
          Map<String, String> addressParts = parseAddress(address);
          String userIDO = addressParts.getOrDefault("userIDO", ""); // 시도
          String userGU = addressParts.getOrDefault("userGU", "");   // 구군
        }

        // 주소에서 시도와 구군 추출
        String userIDO = "";
        String userGU = "";
        if (address != null && !address.isEmpty()) {
          Map<String, String> addressParts = parseAddress(address);
          userIDO = addressParts.getOrDefault("userIDO", ""); // 시도
          userGU = addressParts.getOrDefault("userGU", "");   // 구군
        }

        TB_USERINFO tbUserinfo = TB_USERINFO.builder()
            .userId(id)
            .userNm(name)
            .ageNum(ageNum)
            .sexYn(sexYn)
            .birthYear(birthYear)
            .userHp(userHP)
            .postCd(postno)
            .userAddr(address)
            .userMail(email)
//            .loginPw(Pbkdf2Sha256.encode(password))
            .useYn("1")  //1:사용 0:미사용
            .inDatem(LocalDateTime.now())
            .userIDO(userIDO) // 시도
            .userGU(userGU)   // 구군
            .build();

        userInfoService.save(tbUserinfo);


        result.success = true;
        result.message = "등록이 완료되었습니다.";
        return result;
      } else {
        return result;
      }
    } catch (Exception e) {
      System.out.println(e);

      result.success = false;
      result.message = "에러가발생하였습니다.";
      return result;
    }

  }

  // address를 시도와 구군으로 분리하는 메서드
  private Map<String, String> parseAddress(String address) {
    Map<String, String> result = new HashMap<>();

    if (address != null && !address.isEmpty()) {
      // 공백 기준으로 분리
      String[] parts = address.split("\\s+");

      if (parts.length >= 2) {
        // 첫 번째는 시도
        result.put("userIDO", parts[0].trim());
        // 두 번째는 구군
        result.put("userGU", parts[1].trim());
      }
    }

    // 기본값 설정
    result.putIfAbsent("userIDO", ""); // 시도가 없을 경우 빈 값
    result.putIfAbsent("userGU", ""); // 구군이 없을 경우 빈 값

    return result;
  }

  @PostMapping("/user-auth/searchAccount")
  public AjaxResult IdSearch(@RequestParam("usernm") final String usernm,
                             @RequestParam("mail") final String mail) {

    AjaxResult result = new AjaxResult();

    List<String> user = userRepository.findByFirstNameAndEmailNative(usernm, mail);

    if (!user.isEmpty()) {
      result.success = true;
      result.data = user;
    } else {
      result.success = false;
      result.message = "해당 사용자가 존재하지 않습니다.";
    }
    return result;
  }


  @PostMapping("/user-auth/AuthenticationEmail")
  public AjaxResult PwSearch(@RequestParam("usernm") final String usernm,
                             @RequestParam("mail") final String mail) {

    AjaxResult result = new AjaxResult();

    if (usernm.equals("empty")) {
      sendEmailLogic(mail, "신규사용자");

      result.success = true;
      result.message = "인증 메일이 발송되었습니다.";
      return result;
    }

    int exists = userRepository.existsByUsernameAndEmail(usernm, mail);
    boolean flag = exists > 0;

    if (flag) {
      sendEmailLogic(mail, usernm);

      result.success = true;
      result.message = "인증 메일이 발송되었습니다.";
    } else {
      result.success = false;
      result.message = "해당 사용자가 존재하지 않습니다.";
    }

    return result;
  }


  private void sendEmailLogic(String mail, String prenm) {
    Random random = new Random();
    int randomNum = 100000 + random.nextInt(900000); // 100000부터 999999까지의 랜덤 난수 생성
    String verificationCode = String.valueOf(randomNum); // 정수를 문자열로 변환
    emailService.sendVerificationEmail(mail, prenm, verificationCode);

    tokenStore.put(mail, verificationCode);
    tokenExpiry.put(mail, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3));

  }

  @PostMapping("/user-auth/mobile/SaveAuthenticationEmail")
  public AjaxResult MobilesaveMail(@RequestParam("usernm") final String usernm,
                                   @RequestParam("name") final String saveName,
                                   @RequestParam("mail") final String mail) {

    AjaxResult result = new AjaxResult();

    if (usernm.equals("empty")) {
      saveEmailLogic(mail, saveName);

      result.success = true;
      result.message = "인증 메일이 발송되었습니다.";
      return result;
    }

    int exists = userRepository.existsByUsernameAndEmail(usernm, mail);
    boolean flag = exists > 0;

    if (flag) {
      saveEmailLogic(mail, usernm);

      result.success = true;
      result.message = "인증 메일이 발송되었습니다.";
    } else {
      result.success = false;
      result.message = "해당 사용자가 존재하지 않습니다.";
    }

    return result;
  }

  @PostMapping("/user-auth/SaveAuthenticationEmail")
  public AjaxResult saveMail(@RequestParam("usernm") final String usernm,
                             @RequestParam("saveName") final String saveName,
                             @RequestParam("mail") final String mail) {

    AjaxResult result = new AjaxResult();

    if (usernm.equals("empty")) {
      saveEmailLogic(mail, saveName);

      result.success = true;
      result.message = "인증 메일이 발송되었습니다.";
      return result;
    }

    int exists = userRepository.existsByUsernameAndEmail(usernm, mail);
    boolean flag = exists > 0;

    if (flag) {
      saveEmailLogic(mail, usernm);

      result.success = true;
      result.message = "인증 메일이 발송되었습니다.";
    } else {
      result.success = false;
      result.message = "해당 사용자가 존재하지 않습니다.";
    }

    return result;
  }


  private void saveEmailLogic(String mail, String usernm) {
    Random random = new Random();
    int randomNum = 100000 + random.nextInt(900000); // 100000부터 999999까지의 랜덤 난수 생성
    String verificationCode = String.valueOf(randomNum); // 정수를 문자열로 변환
    emailService.saveVerificationEmail(mail, usernm, verificationCode);

    tokenStore.put(mail, verificationCode);
    tokenExpiry.put(mail, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3));

  }

  @PostMapping("/user-auth/verifyCode")
  public AjaxResult verifyCode(@RequestParam("code") final String code,
                               @RequestParam("mail") final String mail,
                               @RequestParam("password") final String password,
                               @RequestParam("userid") final String userid
  ) {

    AjaxResult result = new AjaxResult();
    result = verifyAuthenticationCode(code, mail);

    if (result.success) {
      String pw = Pbkdf2Sha256.encode(password);


      userRepository.PasswordChange(pw, userid);
      XusersService.PasswordChange(pw, userid, password);

      result.success = true;
      result.message = "비밀번호가 변경되었습니다.";

      return result;
    } else {
      return result;
    }

  }

  private AjaxResult verifyAuthenticationCode(String code, String mail) {

    AjaxResult result = new AjaxResult();

    String storedToken = tokenStore.get(mail);
    if (storedToken != null && storedToken.equals(code)) {
      long expiryTime = tokenExpiry.getOrDefault(mail, 0L);
      if (System.currentTimeMillis() > expiryTime) {
        result.success = false;
        result.message = "인증 코드가 만료되었습니다.";
        tokenStore.remove(mail);
        tokenExpiry.remove(mail);
      } else {
        result.success = true;
        result.message = "비밀번호가 변경되었습니다.";
      }
    } else {
      result.success = false;
      result.message = "인증 코드가 유효하지 않습니다.";
    }
    return result;
  }


  @GetMapping("/user-codes/parent")
  public List<UserCodeDto> getUserCodeByParentId(@RequestParam Integer parentId) {

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
  public List<UserGroup> getUserAuthTypeAll() {
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

  @PostMapping("/authentication")
  public AjaxResult Authentication(@RequestParam(value = "AuthenticationCode") String AuthenticationCode,
                                   @RequestParam(value = "email", required = false) String email,
                                   @RequestParam String type
  ) {
    log.info("코드인증 들어옴(사용자 등록)");
    AjaxResult result = verifyAuthenticationCode(AuthenticationCode, email);

    if (type.equals("new")) {
      if (result.success) {
        flag = true;
        result.message = "인증되었습니다.";

      }

    } else {
      if (result.success) {
        flag_pw = true;
        result.message = "인증되었습니다.";
      }
    }

    return result;
  }

  @GetMapping("/account/userinfo")
  @ResponseBody
  public AjaxResult getUserInfos(Authentication auth) {
    AjaxResult result = new AjaxResult();

    try {
      User user = (User) auth.getPrincipal();
      String username = user.getFirst_name();

      Map<String, String> data = new HashMap<>();
      data.put("username", username);

      result.success = (true);
      result.data = (data);
    } catch (Exception e) {
      result.success = (false);
      result.message = ("사용자 정보를 가져오는 중 오류가 발생했습니다.");
    }

    return result; // JSON 형식으로 반환
  }

}