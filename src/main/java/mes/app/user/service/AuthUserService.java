package mes.app.user.service;

import mes.domain.entity.User;
import mes.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthUserService {
    @Autowired
    private UserRepository userRepository;

    /**
     * 사용자 정보를 기반으로 사용자를 생성하거나 업데이트합니다.
     *
     * @param userInfo 소셜 제공자로부터 받은 사용자 정보
     * @param provider 소셜 로그인 제공자 이름
     */
    public void createOrUpdateUser(Map<String, Object> userInfo, String provider) {
        String email = (String) userInfo.get("email");        // 사용자 이메일
        String name = (String) userInfo.get("name");          // 사용자 이름
        String socialId = (String) userInfo.get("id");        // 소셜 제공자 ID

        // 이메일 기준으로 사용자 조회
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            // 신규 사용자 등록
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirst_name(name);
//            newUser.setProvider(provider); // 소셜 제공자 이름
//            newUser.setSocialId(socialId); // 소셜 제공자 ID
//            newUser.setCreatedAt(new Timestamp(System.currentTimeMillis())); // 가입 시간
            newUser.setLastLogin(new Timestamp(System.currentTimeMillis())); // 첫 로그인 시간
            userRepository.save(newUser);
        } else {
            // 기존 사용자 정보 업데이트
            User user = existingUser.get();
            user.setLastLogin(new Timestamp(System.currentTimeMillis())); // 마지막 로그인 시간 갱신
//            user.setProvider(provider); // 제공자 정보 업데이트
//            user.setSocialId(socialId); // 소셜 ID 업데이트
            userRepository.save(user);
        }
    }

    public boolean isUserRegistered(Object email) {
        return userRepository.existsByEmail(email);
    }
}


