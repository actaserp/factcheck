package mes.app.user.enums;

public enum SocialProvider {
    KAKAO,
    NAVER;

    // 유효성 확인
    public static boolean isValid(String provider) {
        for (SocialProvider sp : values()) {
            if (sp.name().equalsIgnoreCase(provider)) {
                return true;
            }
        }
        return false;
    }

    // 문자열을 Enum으로 변환
    public static SocialProvider fromString(String provider) {
        for (SocialProvider sp : values()) {
            if (sp.name().equalsIgnoreCase(provider)) {
                return sp;
            }
        }
        throw new IllegalArgumentException("Invalid social provider: " + provider);
    }
}
