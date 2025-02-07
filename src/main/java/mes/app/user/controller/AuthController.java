package mes.app.user.controller;

import lombok.extern.slf4j.Slf4j;
import mes.app.account.AccountController;
import mes.app.account.service.TB_USERINFOService;
import mes.app.system.service.UserService;
import mes.app.user.enums.SocialProvider;
import mes.app.user.service.SocialLoginService;
import mes.domain.entity.User;
import mes.domain.entity.actasEntity.TB_USERINFO;
import mes.domain.model.AjaxResult;
import mes.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private NaverLoginProvider naverLoginProvider;

    @Autowired
    private KakaoLoginProvider kakaoLoginProvider;

    @Autowired
    private SocialLoginService socialLoginService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    AccountController accountController;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TB_USERINFOService userInfoService;

    @Autowired
    private UserService userService;


    @Resource(name="authenticationManager")
    private AuthenticationManager authManager;

    // 소셜 로그인 제공자 Map: SocialProvider(Enum)를 키로 사용하고, SocialLoginProvider 구현체를 값으로 가짐
    private final Map<SocialProvider, SocialLoginProvider> providerMap;

    /**
     * 생성자를 통해 Map<SocialProvider, SocialLoginProvider>를 주입받습니다.
     * 이 Map은 Google, Kakao 등의 소셜 로그인 제공자들을 관리합니다.
     */
    @Autowired
    public AuthController(Map<SocialProvider, SocialLoginProvider> providerMap) {
        this.providerMap = providerMap;
    }
    /**
     * 클라이언트 요청에 따라 해당 소셜 로그인 제공자의 인증 URL로 리다이렉트합니다.
     * @param provider 클라이언트가 요청한 소셜 로그인 제공자 이름 (naver, kakao)
     * @return 302 Found 응답과 함께 리다이렉션 URL을 포함한 헤더 반환
     */
    @GetMapping("/{provider}")
    public ResponseEntity<?> redirectToSocialLogin(@PathVariable String provider) {
//        log.info("소셜 로그인 요청 수신 - provider: {}", provider);

        try {
            SocialProvider socialProvider = SocialProvider.valueOf(provider.toUpperCase());
           // log.info("소셜 로그인 provider 변환 성공: {}", socialProvider);

            return Optional.ofNullable(providerMap.get(socialProvider))
                .map(loginProvider -> {
                  //  log.info("SocialLoginProvider: {} - 인증 URL 생성 중", loginProvider.getClass().getSimpleName());
                    String authorizationUrl = loginProvider.getAuthorizationUrl(null, null, Map.of());
                    //log.info("생성된 인증 URL: {}", authorizationUrl);
                    return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", authorizationUrl)
                        .build();
                })
                .orElseGet(() -> {
                    log.error("ProviderMap에 설정되지 않은 소셜 로그인 provider: {}", socialProvider);
                    return ResponseEntity.badRequest().body("Provider not configured: " + provider);
                });

        } catch (IllegalArgumentException e) {
            log.error("지원하지 않는 소셜 로그인 provider: {}", provider, e);
            return ResponseEntity.badRequest().body("Unsupported provider: " + provider);
        }
    }

    @GetMapping("/{provider}/callback")
    public ModelAndView handleSocialCallback(
        @PathVariable String provider,
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String error,
        HttpServletRequest request
    ) {
        ModelAndView mv = new ModelAndView();

        try {
            if (error != null) {
                log.error("소셜 로그인 실패: provider={}, error={}", provider, error);
                mv.setViewName("redirect:/login?error=social_login_failed");
                return mv;
            }

            if (code == null) {
                log.error("소셜 로그인 실패: 인증 코드 누락");
                mv.setViewName("redirect:/login?error=missing_code");
                return mv;
            }
            String accessToken;
            SocialLoginProvider loginProvider;

            if ("naver".equalsIgnoreCase(provider)) {
//                log.info("네이버 로그인 처리 시작");
                loginProvider = naverLoginProvider;
            } else if ("kakao".equalsIgnoreCase(provider)) {
//                log.info("카카오 로그인 처리 시작");
                loginProvider = kakaoLoginProvider;
            } else {
                log.error("지원하지 않는 소셜 로그인 provider: {}", provider);
                mv.setViewName("redirect:/login?error=unsupported_provider");
                return mv;
            }

            // Access Token 가져오기
            accessToken = loginProvider.getAccessToken(code);
//            log.info("{} Access Token: {}", provider, accessToken);

            // 사용자 정보 가져오기
            Map<String, Object> userInfo = socialLoginService.getUserInfo(loginProvider, accessToken);

            if (userInfo == null || userInfo.isEmpty()) {
                log.warn("사용자 정보를 가져올 수 없습니다.");
                mv.setViewName("redirect:/login?error=failed_to_retrieve_userinfo");
                return mv;
            }

          /*  // "response" 키에서 데이터 추출
            Map<String, Object> responseData;
            if ("naver".equalsIgnoreCase(provider)) {
                responseData = (Map<String, Object>) userInfo.get("response");  // 네이버 응답 구조
            } else {
                responseData = userInfo;  // 카카오는 response 키 없이 바로 정보가 담겨있음
            }

            if (responseData == null || responseData.isEmpty()) {
                log.error("사용자 정보 응답 데이터가 없습니다.");
                mv.setViewName("redirect:/login?error=missing_response_data");
                return mv;
            }
*/
            // 소셜 로그인 사용자 정보 매핑
            String email;
            String name;
            String userId;

            if ("kakao".equalsIgnoreCase(provider)) {
                // 카카오는 `kakao_account.email`에서 이메일 가져오기
                Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
                email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
                name = kakaoAccount != null && kakaoAccount.containsKey("profile")
                    ? (String) ((Map<String, Object>) kakaoAccount.get("profile")).get("nickname")
                    : null;
                userId = String.valueOf(userInfo.get("id")); // Long -> String 변환
            } else if ("naver".equalsIgnoreCase(provider)) {
                // 네이버는 `response.email`에서 이메일 가져오기
                Map<String, Object> responseMap = (Map<String, Object>) userInfo.get("response");
                email = responseMap != null ? (String) responseMap.get("email") : null;
                name = responseMap != null ? (String) responseMap.get("name") : null;
                userId = responseMap != null ? (String) responseMap.get("id") : null; // String 그대로 사용
            } else {
                throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자: " + provider);
            }

            String sessionToken = accessToken; // 여기에서 Access Token을 세션 토큰으로 사용

            if (email == null) {
                log.error("소셜 로그인 callback에서 이메일을 확인할 수 없습니다.");
                mv.setViewName("redirect:/login?error=email_not_found");
                return mv;
            }
            //log.info("소셜 로그인 - email: {}, name: {}, userId: {}, sessionToken: {}", email, name, userId, sessionToken);

            // 사용자 등록 여부 확인
            Optional<User> isUserRegistered = userRepository.findByEmail(email);
            //log.info("사용자 등록 여부 확인을 위해 이메일 검색: {}", email);

            if (isUserRegistered.isEmpty()) {
                log.info("신규 사용자: {}, 추가 정보 입력 페이지로 리다이렉트", email);

                // ModelAndView에 데이터 추가
                mv.addObject("email", email);
                mv.addObject("name", name);
                mv.addObject("userId", userId);
                mv.addObject("sessionToken", sessionToken);
                mv.addObject("provider",provider);
                mv.setViewName("mobile/UserInfo");

                log.info("리다이렉트 성공: /mobile/UserInfo, 전달된 데이터: email={}, name={}, userId={}, sessionToken={}, provider={}",
                    email, name, userId, sessionToken,provider);
                return mv;
            }

            // 내부 로그인 처리
            AjaxResult result = accountController.postLogin(email, null, provider, request);

            if (result.data instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) result.data;
                String resultCode = (String) data.get("code");

                if ("OK".equals(resultCode)) {
                    //log.info("로그인 성공: {}", email);
                    mv.setViewName("redirect:/");
                    return mv;
                } else {
                    log.warn("로그인 실패: {}", data);
                    mv.setViewName("redirect:/login?error=login_failed");
                    return mv;
                }
            } else {
                log.error("Unexpected data type: {}", result.data.getClass());
                mv.setViewName("redirect:/login?error=unexpected_data");
                return mv;
            }

        } catch (Exception e) {
            log.error("소셜 로그인 처리 중 오류 발생: provider={}, error={}", provider, e);
            mv.setViewName("redirect:/login?error=social_login_failed");
            return mv;
        }
    }

    private SocialLoginProvider getProvider(String provider) {
        return Optional.ofNullable(SocialProvider.valueOf(provider.toUpperCase()))
            .map(providerMap::get)
            .orElseThrow(() -> new IllegalArgumentException("Unsupported or unconfigured provider: " + provider));
    }

    @PostMapping("/social/save")
    public ResponseEntity<Map<String, Object>> socialUserSave(
                                                 @RequestParam(value = "name") String name,
                                                 @RequestParam(value = "userHP") String userHP,
                                                 @RequestParam(value = "email") String email,
                                                 @RequestParam(value = "birthYear") String birthYear,
                                                 @RequestParam(value = "sexYn") String sexYn, // 필수
                                                 @RequestParam(value = "postno") String postno, // 필수
                                                 @RequestParam(value = "address1") String address, // 필수
                                                 @RequestParam(value = "userId")String userId,
                                                 @RequestParam(value = "sessionToken") String sessionToken,
                                                 @RequestParam(value = "provider" )String provider
                                                 ) {
//        log.info("소셜 사용자 정보 들어옴");
//        log.info("수신된 데이터 - name: {}, userHP: {}, email: {}, birthYear: {}, sexYn: {}, postno: {}, address: {}, userId: {}, sessionToken: {}",
//            name, userHP, email, birthYear, sexYn, postno, address, userId, sessionToken);
        ModelAndView mv = new ModelAndView();
        Map<String, Object> response = new HashMap<>();
        try {
            // 사용자 정보 저장
            User newUser = User.builder()
                .username(email)
                .phone(userHP)
                .email(email)
                .first_name(name)
                .last_name(name)
                .active(true)
                .is_staff(false)
                .date_joined(new Timestamp(System.currentTimeMillis()))
                .superUser(false)
                .build();
            userService.save(newUser);
            log.info("newUser저장:{} ", newUser);

            jdbcTemplate.execute("SET IDENTITY_INSERT user_profile ON");
            // UserProfile 저장 (JDBC 사용)
            String sql = "INSERT INTO user_profile (_created, lang_code, Name, UserGroup_id, User_id) VALUES (?,?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                new Timestamp(System.currentTimeMillis()), // 현재 시간
                "ko-KR", // lang_code (예: 한국어)
                name, // Name (사용자 이름)
                36 ,// marketing_manager (모바일 관리자)
                newUser.getId() // User_id
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
                .userId(email)
                .userNm(name)
                .ageNum(ageNum)
                .sexYn(sexYn)
                .birthYear(birthYear)
                .userHp(userHP)
                .postCd(postno)
                .userAddr(address)
                .userMail(email)
                .useYn("1")	//1:사용 0:미사용
                .inDatem(LocalDateTime.now())
                .ssoCd(provider)
                .userIDO(userIDO) // 시도
                .userGU(userGU)   // 구군
                .inUserId(userId)
                .tokenInfo(sessionToken)
                .build();

            userInfoService.save(tbUserinfo);
//            log.info("TB_USERINFO 저장: {}", tbUserinfo);
            // 성공 응답 반환
            response.put("success", true);
            response.put("message", "사용자 정보가 성공적으로 저장되었습니다.");
            response.put("redirectUrl", "/");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("소셜 사용자 정보 저장 중 오류 발생: {}", e.getMessage());

            response.put("success", false);
            response.put("message", "사용자 정보 저장 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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



}