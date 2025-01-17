package mes.app.user.controller;

import lombok.extern.slf4j.Slf4j;
import mes.app.user.enums.SocialProvider;
import mes.app.user.service.AuthUserService;
import mes.app.user.service.SocialLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SocialLoginService socialLoginService;

    @Autowired
    private AuthUserService authUserService;

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
     * @param provider 클라이언트가 요청한 소셜 로그인 제공자 이름 (google, kakao 등)
     * @return 302 Found 응답과 함께 리다이렉션 URL을 포함한 헤더 반환
     */
    @GetMapping("/{provider}")
    public ResponseEntity<?> redirectToSocialLogin(@PathVariable String provider) {
        SocialProvider socialProvider;

        // 요청받은 provider 문자열을 Enum으로 변환 (소문자를 대문자로 처리)
        try {
            socialProvider = SocialProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 지원하지 않는 provider일 경우 400 Bad Request 응답
            return ResponseEntity.badRequest().body("Unsupported provider: " + provider);
        }

        // 해당 소셜 로그인 제공자에 맞는 SocialLoginProvider 구현체 가져오기
        SocialLoginProvider loginProvider = providerMap.get(socialProvider);
        if (loginProvider == null) {
            // Map에 설정되지 않은 provider 요청 시 400 Bad Request 응답
            return ResponseEntity.badRequest().body("Provider not configured: " + provider);
        }

        // 해당 제공자에 맞는 인증 URL 생성
        String authorizationUrl = loginProvider.getAuthorizationUrl(null, null, Map.of());

        // 클라이언트를 인증 URL로 리다이렉트
        return ResponseEntity.status(HttpStatus.FOUND) // HTTP 302 리다이렉션
                .header("Location", authorizationUrl)
                .build();
    }

    @PostMapping("/{provider}/callback")
    public ResponseEntity<?> handleSocialCallback(
            @PathVariable String provider,
            @RequestParam String code) {
        try {
            // 1. 소셜 로그인 제공자 가져오기
            SocialLoginProvider loginProvider = getProvider(provider);

            // 2. 소셜 로그인 제공자로부터 사용자 정보 가져오기
            Map<String, Object> userInfo = socialLoginService.getUserInfo(loginProvider, code);

            // 3. 등록 여부 확인
            boolean isUserRegistered = authUserService.isUserRegistered(userInfo.get("email"));
            if (!isUserRegistered) {
                // 등록이 필요한 사용자: 가져온 정보를 반환
                return ResponseEntity.status(HttpStatus.OK)
                        .body(Map.of(
                                "email", userInfo.get("email"),
                                "name", userInfo.get("name"),
                                "isSocialUser", true
                        ));
            }

            // 4. 기존 사용자는 로그인 처리
            return ResponseEntity.ok("Login Successful");

        } catch (IllegalArgumentException e) {
            log.error("Invalid provider: {}", provider, e);
            return ResponseEntity.badRequest().body("Unsupported provider: " + provider);
        } catch (Exception e) {
            log.error("Error during social login callback for provider {}: {}", provider, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Social login failed");
        }
    }


    private SocialLoginProvider getProvider(String provider) {
        try {
            SocialProvider socialProvider = SocialProvider.valueOf(provider.toUpperCase());
            SocialLoginProvider loginProvider = providerMap.get(socialProvider);
            if (loginProvider == null) {
                throw new IllegalArgumentException("Provider not configured: " + provider);
            }
            return loginProvider;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported provider: " + provider, e);
        }
    }



}