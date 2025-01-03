package mes.app.system;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;
import mes.app.UtilClass;
import mes.app.account.service.TB_RP945_Service;
import mes.app.account.service.TB_xusersService;
import mes.domain.DTO.TB_RP945Dto;
import mes.domain.entity.TB_RP945;
import mes.domain.entity.UserCode;
import mes.domain.entity.actasEntity.TB_XUSERS;
import mes.domain.entity.actasEntity.TB_XUSERSId;
import mes.domain.repository.*;
import mes.domain.repository.actasRepository.TB_XuserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.sql.In;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mes.app.system.service.UserService;
import mes.domain.entity.RelationData;
import mes.domain.entity.User;
import mes.domain.model.AjaxResult;
import mes.domain.security.Pbkdf2Sha256;
import mes.domain.services.CommonUtil;
import mes.domain.services.SqlRunner;

@Slf4j
@RestController
@RequestMapping("/api/system/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RelationDataRepository relationDataRepository;

	@Autowired
	SqlRunner sqlRunner;
	@Autowired
	private TB_RP940Repository tB_RP940Repository;
	@Autowired
	private TB_RP945Repository tB_RP945Repository;

	@Autowired
	TB_xusersService XusersService;
	@Autowired
	TB_XuserRepository xuserstRepository;
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	private UserCodeRepository userCodeRepository;


	// 사용자 리스트 조회
	@GetMapping("/read")
	public AjaxResult getUserList(@RequestParam(value = "keyword", required = false) String pernm,
								  @RequestParam(value = "group", required = false) String userGroupId,
								  @RequestParam(value = "username", required = false) String username,
								  @RequestParam(value = "email", required = false) String email,
								  Authentication auth) {
		log.info("Received Parameters - group: {}, keyword: {}, username: {}", userGroupId, pernm, username);

		AjaxResult result = new AjaxResult();
		User user = (User) auth.getPrincipal();
		boolean superUser = user.getSuperUser();

		if (!superUser) {
			superUser = "dev".equals(user.getUserProfile().getUserGroup().getCode());
		}

		List<Map<String, Object>> items = userService.getUserList(superUser, username, email, pernm, userGroupId);
		result.data = items;

		return result;
	}

	// 사용자 상세정보 조회
	@GetMapping("/detail")
	public AjaxResult getUserDetail(@RequestParam(value = "id", required = false) String id) {
		AjaxResult result = new AjaxResult();
		log.info("id: {}", id);

		try {
			if (id != null && !id.isEmpty()) {
				// ID로 특정 사용자 정보 조회
				Map<String, Object> userDetail = userService.getUserDetailById(id);

				result.success = true;
				result.data = userDetail;
				result.message = "데이터 조회 성공";
			} else {
				result.success = false;
				result.message = "유효한 ID가 제공되지 않았습니다.";
			}
		} catch (Exception e) {
			result.success = false;
			result.message = "데이터를 가져오는 중 오류가 발생했습니다.";
		}

		return result;
	}
	private Map<String, String> splitAddress(String fullAddress) {
		Map<String, String> addressParts = new HashMap<>();
		if (fullAddress != null && !fullAddress.isEmpty()) {
			String[] parts = fullAddress.split("\\|", 2); // "|"를 기준으로 분리
			addressParts.put("address1", parts[0].trim()); // 첫 번째 부분
			addressParts.put("address2", parts.length > 1 ? parts[1].trim() : ""); // 두 번째 부분
		} else {
			addressParts.put("address1", "");
			addressParts.put("address2", "");
		}
		return addressParts;
	}

	/*@GetMapping("/detail")
	public AjaxResult getUserDetail(
			@RequestParam(value="id") Integer id,
			HttpServletRequest request) {

		Map<String, Object> item = this.userService.getUserDetail(id);
		AjaxResult result = new AjaxResult();
		result.data = item;
		return result;
	}*/

	// 사용자 그룹 조회
	@GetMapping("/user_grp_list")
	public AjaxResult getUserGrpList(
			@RequestParam(value="id") Integer id,
			HttpServletRequest request) {

		List<Map<String, Object>> items = this.userService.getUserGrpList(id);
		AjaxResult result = new AjaxResult();
		result.data = items;
		return result;
	}

	@PostMapping("/saveOrUpdate")
	@Transactional
	public AjaxResult saveUser(@RequestBody Map<String, Object> requestData, Authentication auth) {
		AjaxResult result = new AjaxResult();

		try {
			// 요청 데이터 로그
			log.info("Received Request Data: {}", requestData);

			// 데이터 매핑
			Integer id = (requestData.get("id") != null && !requestData.get("id").toString().isEmpty()) ? Integer.valueOf(requestData.get("id").toString()) : null;
			String loginId = (String) requestData.get("login_id");
			String name = (String) requestData.get("pernm");
			String email = (String) requestData.get("email");
			Integer userGroupId = Integer.valueOf(requestData.get("UserGroup_id").toString());
			String password = (String) requestData.get("password");
			String phone = (String) requestData.get("Phone");
			Boolean isActive = false;
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
						SET Name = ?, UserGroup_id = ?
						WHERE User_id = ?
					""";
			rowsUpdated = jdbcTemplate.update(sql,
					name,             // Name (사용자 이름)
					userGroupId,      // UserGroup_id (사용자 그룹 ID)
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
					.passwd1(password != null ? password : user.getPassword())
					.passwd2(password != null ? password : user.getPassword())
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

	// user 삭제
	@Transactional
	@PostMapping("/delete")
	public AjaxResult deleteUser(@RequestParam("id") String id,
								 @RequestParam(value = "username", required = false) String username,
								 @RequestParam(value = "auth", defaultValue = "false") boolean auth) {
		AjaxResult result = new AjaxResult();
		//log.info("삭제 요청 처리 시작 - id: {}, username: {}", id, username);

		if (auth) {
			result.success = false;
			result.message = "슈퍼유저는 수정 및 삭제가 불가능합니다.";
			return result;
		}

		// username으로 TB_XUSERS 삭제
		if (username != null && !username.trim().isEmpty()) {
			String cleanedUsername = UtilClass.removeBrackers(username);
			xuserstRepository.deleteById_userid(cleanedUsername);
			//log.info("TB_XUSERS에서 username {} 삭제 완료", cleanedUsername);
		} else {
			//log.warn("username이 제공되지 않아 TB_XUSERS 삭제를 건너뜁니다.");
		}

		// id로 User 삭제
		if (id != null && !id.trim().isEmpty()) {
			Integer userId = Integer.parseInt(UtilClass.removeBrackers(id));
			userRepository.deleteById(userId);
			//log.info("User 엔티티에서 ID {} 삭제 완료", userId);
		} else {
			//log.warn("id가 제공되지 않아 User 삭제를 건너뜁니다.");
		}

		result.success = true;
		result.message = "사용자 삭제가 완료되었습니다.";
		return result;
	}

	@PostMapping("/modfind")
	public AjaxResult getBtId(@RequestBody String userid){

		///userid = new UtilClass().removeBrackers(userid);
		AjaxResult result = new AjaxResult();

		List<TB_RP945> tbRp945 =  tB_RP945Repository.findByUserid(userid);

		if(!tbRp945.isEmpty()){
			result.success = true;
			result.data = tbRp945;
		}else{
			result.success = false;
			result.message = "해당 유저에 대한 권한상세정보가 없습니다.";
		}



		return result;
	}


	// user 패스워드 셋팅
	@PostMapping("/passSetting")
	@Transactional
	public AjaxResult userPassSetting(
			@RequestParam(value="id", required = false) Integer id,
			@RequestParam(value="pass1", required = false) String loginPwd,
			@RequestParam(value="pass2", required = false) String loginPwd2,
			Authentication auth
	) {

		User user = null;
		AjaxResult result = new AjaxResult();

		if (StringUtils.hasText(loginPwd)==false | StringUtils.hasText(loginPwd2)==false) {
			result.success=false;
			result.message="The verification password is incorrect.";
			return result;
		}

		if(loginPwd.equals(loginPwd2)==false) {
			result.success=false;
			result.message="The verification password is incorrect.";
			return result;
		}

		user = this.userRepository.getUserById(id);
		user.setPassword(Pbkdf2Sha256.encode(loginPwd));
		this.userRepository.save(user);

		return result;
	}

	@PostMapping("/save_user_grp")
	@Transactional
	public AjaxResult saveUserGrp(
			@RequestParam(value="id") Integer id,
			@RequestBody MultiValueMap<String,Object> Q,
			Authentication auth
	) {

		User user = (User)auth.getPrincipal();;

		AjaxResult result = new AjaxResult();

		List<Map<String, Object>> items = CommonUtil.loadJsonListMap(Q.getFirst("Q").toString());

		List<RelationData> rdList = this.relationDataRepository.findByDataPk1AndTableName1AndTableName2(id,"auth_user", "user_group");

		// 등록된 그룹 삭제
		for (int i = 0; i < rdList.size(); i++) {
			this.relationDataRepository.deleteById(rdList.get(i).getId());
		}

		this.relationDataRepository.flush();
		for (int i = 0; i< items.size(); i++) {

			String check = "";

			if (items.get(i).get("grp_check") != null) {
				check = items.get(i).get("grp_check").toString();
			}

			if (check.equals("Y")) {
				RelationData rd = new RelationData();
				rd.setDataPk1(id);
				rd.setTableName1("auth_user");
				rd.setDataPk2(Integer.parseInt(items.get(i).get("grp_id").toString()));
				rd.setTableName2("user_group");
				rd.setRelationName("auth_user-user_group");
				rd.setChar1("Y");
				rd.set_audit(user);

				this.relationDataRepository.save(rd);
			}
		}


		return result;
	}
}
