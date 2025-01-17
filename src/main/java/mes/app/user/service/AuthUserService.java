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
     * 소셜 로그인 사용자 생성 또는 업데이트
     * @param userInfo 소셜 제공자로부터 받은 사용자 정보
     */
    public void createOrUpdateUser(Map<String, Object> userInfo) {
        String email = (String) userInfo.get("email");        // 사용자 이메일
        String name = (String) userInfo.get("name");          // 사용자 이름
        String socialId = (String) userInfo.get("socialId");  // 소셜 제공자 ID
        String provider = (String) userInfo.get("provider");  // 소셜 제공자 이름

        // 이메일 기준으로 사용자 조회
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            // 신규 사용자 등록
            User newUser = new User();
            newUser.setEmail(email);        //이메일(소셜 아이디)
            newUser.setFirst_name(name);    //이름
            newUser.setUsername(socialId);  //소셜 아이디
            //newUser.setProvider(provider);
            newUser.setDate_joined(new Timestamp(System.currentTimeMillis())); // 가입 시간
            userRepository.save(newUser);
        } else {
            // 기존 사용자 정보 업데이트
            User user = existingUser.get();
            user.setLastLogin(new Timestamp(System.currentTimeMillis())); // 마지막 로그인 시간 갱신
            userRepository.save(user);
        }
    }
}

