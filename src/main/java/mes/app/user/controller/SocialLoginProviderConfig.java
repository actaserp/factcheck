package mes.app.user.controller;

import mes.app.user.enums.SocialProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SocialLoginProviderConfig {

    private final GoogleLoginProvider googleLoginProvider;
    private final KakaoLoginProvider kakaoLoginProvider;
    private final NaverLoginProvider naverLoginProvider;

    /**
     * 생성자를 통해 GoogleLoginProvider와 KakaoLoginProvider 의존성을 주입받습니다.
     * 이를 통해 Spring에서 관리되는 Provider 빈을 활용할 수 있습니다.
     */
    public SocialLoginProviderConfig(GoogleLoginProvider googleLoginProvider, KakaoLoginProvider kakaoLoginProvider, NaverLoginProvider naverLoginProvider){
        this.googleLoginProvider = googleLoginProvider;
        this.kakaoLoginProvider = kakaoLoginProvider;
        this.naverLoginProvider = naverLoginProvider;
    }

    /**
     * 소셜 로그인 제공자를 관리하는 Map을 Spring Bean으로 등록합니다.
     * Map의 키는 SocialProvider(Enum)로, 값은 각각의 소셜 로그인 제공자 구현체입니다.
     * 이를 통해 AuthController 등에서 제공자를 쉽게 참조할 수 있습니다.
     *
     * @return 소셜 로그인 제공자 Map
     */
    @Bean
    public Map<SocialProvider, SocialLoginProvider> providerMap() {
        return Map.of(
                SocialProvider.GOOGLE, googleLoginProvider, // Google Provider 추가
                SocialProvider.KAKAO, kakaoLoginProvider ,   // Kakao Provider 추가
                SocialProvider.NAVER, naverLoginProvider
        );
    }
}
