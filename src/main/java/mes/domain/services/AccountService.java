package mes.domain.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import mes.domain.entity.User;
import mes.domain.repository.UserRepository;
import mes.domain.security.Pbkdf2Sha256;
import org.springframework.transaction.annotation.Transactional;

/*인터페이스 분리는 추후에 고민하자*/
@Service
public class AccountService {
	
	@Autowired
	UserRepository userRepository;	

	@Autowired
	SqlRunner sqlRunner;
		
	public User getUser(String username) {
		User user =null;
		if(username==null || username.length()==0 ) {
			return user;
		}			
		
		Optional<User> optUser = this.userRepository.findIsActiveUserByUsername(username);
		
		if(optUser.isEmpty()==false) {
			user = optUser.get();
		}	
		
		return user;		
	}
	
	public boolean checkUserPassword(String plainText, User user) {
		
		String hashedPassword = user.getPassword();		
		boolean result = Pbkdf2Sha256.verification(plainText, hashedPassword);		
		return result;	
	}

// 로그인&로그아웃 시 login_log 테이블에 이력 저장
@Transactional
public void saveLoginLog(String type, Authentication auth) throws UnknownHostException {

	Object principal = auth.getPrincipal(); // Principal 객체 가져오기

	// principal 타입 확인
	if (principal instanceof User user) {
		// principal이 User 객체인 경우
		MapSqlParameterSource paramMap = new MapSqlParameterSource();
		paramMap.addValue("type", type);
		paramMap.addValue("IPAddress", InetAddress.getLocalHost().getHostAddress());
		paramMap.addValue("UserId", user.getId());

		String sql = """
            INSERT INTO login_log(Type, IPAddress, _created, User_id)
            VALUES (:type, :IPAddress, GETDATE(), :UserId);
            """;

		try {
			int rowsAffected = this.sqlRunner.execute(sql, paramMap);
			System.out.println("Rows affected: " + rowsAffected);
		} catch (Exception e) {
			System.err.println("Failed to save login log: " + e.getMessage());
			e.printStackTrace();
		}
	} else if (principal instanceof String username) {
		// principal이 String인 경우 (사용자 이름만 존재)
		System.err.println("Principal is a String: " + username + ". Login log not saved.");
	} else {
		throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
	}
}

}
