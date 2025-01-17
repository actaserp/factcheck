package mes.app.user.controller;

import lombok.extern.slf4j.Slf4j;
import mes.app.user.enums.SocialProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

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
}
