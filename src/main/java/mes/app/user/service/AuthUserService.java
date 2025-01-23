package mes.app.user.service;

import lombok.extern.slf4j.Slf4j;
import mes.app.account.service.TB_USERINFOService;
import mes.app.system.service.UserService;
import mes.app.user.enums.SocialProvider;
import mes.domain.entity.User;
import mes.domain.entity.actasEntity.TB_USERINFO;
import mes.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class AuthUserService {
  @Autowired
  private UserRepository userRepository;
  @Autowired
  TB_USERINFOService userInfoService;
  @Autowired
  JdbcTemplate jdbcTemplate;

    @Transactional
    public User saveOrUpdateUser(Map<String, Object> userInfo, SocialProvider provider) {
        Map<String, Object> responseData = (Map<String, Object>) userInfo.get("response");

        // 필요한 필드 추출
        String id = (String) responseData.get("id");
        String name = Optional.ofNullable((String) responseData.get("name")).orElse("Unknown");
        String email = Optional.ofNullable((String) responseData.get("email"))
            .orElseThrow(() -> new IllegalArgumentException("Email is required"));
        String gender = Optional.ofNullable((String) responseData.get("gender")).orElse("U");
      Integer age = Optional.ofNullable((String) responseData.get("age"))
          .map(Integer::valueOf) // String을 Integer로 변환
          .orElse(null); // 값이 없거나 변환 실패 시 null 반환
      String birthday = Optional.ofNullable((String) responseData.get("birthday")).orElse("01-01");
        String birthyear = Optional.ofNullable((String) responseData.get("birthyear")).orElse("1900");
        String mobile = Optional.ofNullable((String) responseData.get("mobile")).orElse("Unknown");

        // 기존 사용자 조회
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // 기존 사용자 업데이트
            User user = existingUser.get();
            user.setFirst_name(name);
            user.setPhone(mobile);
            user.setActive(true);
            userRepository.save(user);
            log.info("기존 사용자 업데이트: {}", user);
            return user;
        } else {
            // 새로운 사용자 저장
            User newUser = User.builder()
                .username(email)
                .phone(mobile)
                .email(email)
                .first_name(name)
                .last_name(name)
                .active(true)
                .is_staff(false)
                .date_joined(new Timestamp(System.currentTimeMillis()))
                .superUser(false)
                .build();
            userRepository.save(newUser);
            log.info("새로운 사용자 저장: {}", newUser);

            jdbcTemplate.execute("SET IDENTITY_INSERT user_profile ON");
            // UserProfile 저장 (JDBC 사용)
            String sql = "INSERT INTO user_profile (_created, lang_code, Name, UserGroup_id, User_id) VALUES (?,?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                new Timestamp(System.currentTimeMillis()), // 현재 시간
                "ko-KR", // lang_code (예: 한국어)
                name, // Name (사용자 이름)
                36 ,// marketing_manager (마케팅 관리자)
                newUser.getId() // User_id
            );
            jdbcTemplate.execute("SET IDENTITY_INSERT user_profile OFF");

            // `TB_USERINFO` 저장
            TB_USERINFO tbUserinfo = TB_USERINFO.builder()
                .inUserId(id)
                .inUserNm(name)
                .userMail(email)
                .sexYn(gender)
                .ageNum(age)
                .birthYear(birthyear)
                .userHp(mobile)
                .ssoCd(provider.name())
                .build();
            userInfoService.save(tbUserinfo);
            log.info("TB_USERINFO 저장: {}", tbUserinfo);

            return newUser;
        }
    }

  public void createOrUpdateUser(Map<String, Object> userInfo, String simpleName) {
  }

}


