package mes.app.system;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;
import mes.app.UtilClass;
import mes.app.account.service.TB_RP945_Service;
import mes.domain.DTO.TB_RP945Dto;
import mes.domain.entity.TB_RP945;
import mes.domain.entity.UserCode;
import mes.domain.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.sql.In;
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
	TB_RP945_Service tbRp945Service;
	@Autowired
	private UserCodeRepository userCodeRepository;


	// 사용자 리스트 조회
	@GetMapping("/read")
	public AjaxResult getUserList(@RequestParam(value = "cltnm", required = false) String cltnm, // 업체명
								  @RequestParam(value = "prenm", required = false) String prenm, // 대표자
								  @RequestParam(value = "biztypenm", required = false) String biztypenm, // 업태
								  @RequestParam(value = "bizitemnm", required = false) String bizitemnm, // 종목
								  @RequestParam(value = "email", required = false) String email,
								  @RequestParam(value = "spjangcd", required = false) String spjangcd,
								  Authentication auth){
		AjaxResult result = new AjaxResult();
		User user = (User)auth.getPrincipal();
		boolean superUser = user.getSuperUser();

		if (!superUser) {
			superUser = user.getUserProfile().getUserGroup().getCode().equals("dev");
		}

		List<Map<String, Object>> items = this.userService.getUserList(superUser, cltnm, prenm, biztypenm, bizitemnm, email, spjangcd);

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

				// fullAddress 분리하여 address1과 address2로 추가
				if (userDetail.containsKey("fullAddress")) {
					String fullAddress = (String) userDetail.get("fullAddress");
					Map<String, String> addressParts = splitAddress(fullAddress);
					userDetail.put("address1", addressParts.get("address1"));
					userDetail.put("address2", addressParts.get("address2"));
				}

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

	/*@PostMapping("/save")
	@Transactional
	public AjaxResult saveUser(){

	}*/


	@PostMapping("/save")
	@Transactional
	public AjaxResult saveUser(
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "login_id") String login_id,
			@RequestParam(value = "Name") String Name,
			@RequestParam(value = "email") String email,
			@RequestParam(value="UserGroup_id") Integer UserGroup_id,
			@RequestParam(value="password", required = false) String password,
			@RequestParam(value="tel", required = false) String tel,
			@RequestParam(value="divinm", required = false) String divinm,
			@RequestParam(value="agencycd", required = false) String agencycd,
			@RequestParam(value="spworkcd") String spworkcd,
			@RequestParam(value="spcompcd") String spcompcd,
			@RequestParam(value="spplancd") String spplancd,
			@RequestParam(value="spworknm") String spworknm,
			@RequestParam(value="spcompnm") String spcompnm,
			@RequestParam(value="spplannm") String spplannm,
			Authentication auth
	){

		//산단 발전소 지역 코드를 공통코드 id로 받지않고 텍스트와 코드값으로 받는이유(spworkcd 등등) --> id로 받으면 공통코드테이블에서 호출해야한다. DB호출은 부담 차라리 값을 많이 넘기자
		AjaxResult result = new AjaxResult();

		UtilClass util = new UtilClass();
		List<Integer> spworkidList = util.parseUserIdsToInt(spworkcd);
		List<Integer> spcompidList = util.parseUserIdsToInt(spcompcd);
		List<Integer> spplanidList = util.parseUserIdsToInt(spplancd);

		List<String> spworknmList = Arrays.asList(spworknm.split(","));
		List<String> spcompnmList = Arrays.asList(spcompnm.split(","));
		List<String> spplannmList = Arrays.asList(spplannm.split(","));

		String sql = null;
		User user = null;
		User loginUser = (User)auth.getPrincipal();
		Timestamp today = new Timestamp(System.currentTimeMillis());
		MapSqlParameterSource dicParam = new MapSqlParameterSource();
		boolean username_chk = this.userRepository.findByUsername(login_id).isEmpty();

		if(id==null){
			if (username_chk == false){
				result.success = false;
				result.message = "중복된 사번이 존재합니다.";
				return result;
			}
			user = new User();
			user.setPassword(Pbkdf2Sha256.encode(password));
			user.setSuperUser(false);
			user.setLast_name("");
			user.setIs_staff(false);

			dicParam.addValue("loginUser", loginUser.getId());

			sql = """
		        	INSERT INTO user_profile 
		        	("_created", "_creater_id", "User_id", "lang_code", "Name", "UserGroup_id" ) 
		        	VALUES (now(), :loginUser, :User_id, :lang_code, :name, :UserGroup_id )
		        """;
		}else {



			user = this.userRepository.getUserById(id);

			//만약 있으면 수정, pk2개라서 관리하기 불편, 다 지우고 새로추가, 위치가 왜 여기냐 --> user객체 저장 혹은 수정전에 username을 가져와야함
			List<TB_RP945> rp945List = tB_RP945Repository.findByUserid(user.getUsername());
			if(!rp945List.isEmpty()){
				tB_RP945Repository.deleteByUserid(user.getUsername());
			}

			sql = """
					update user_profile set
					     	"lang_code" = :lang_code, "Name" = :name
					     	,   "UserGroup_id" = :UserGroup_id
					where "User_id" = :User_id
		        """;
		}

		user.setUsername(login_id);
		user.setFirst_name(Name);
		user.setEmail(email);
		user.setDate_joined(today);
		user.setActive(true);
		user.setDivinm(divinm);
		user.setTel(tel);
		user.setAgencycd(agencycd);

		user = this.userRepository.save(user);

		dicParam.addValue("name", Name);
		dicParam.addValue("UserGroup_id", UserGroup_id);
		dicParam.addValue("lang_code", "ko-KR");
		dicParam.addValue("User_id", user.getId());

		this.sqlRunner.execute(sql, dicParam);

		result.data = user;


		//신청순번의 최대값을 구한후 +1을 하고 문자열로 바꿔줌
		//이거 반복문 안에 넣으면 db호출이 너무 많다.
		String RawAskSeq = tB_RP945Repository.findMaxAskSeq();
		RawAskSeq = (RawAskSeq != null) ? RawAskSeq : "0";

		int AskSeqInt = Integer.parseInt(RawAskSeq) + 1;


		Map<Integer, UserCode> spworkCodes = userCodeRepository.findAllById(spworkidList)
				.stream().collect(Collectors.toMap(UserCode::getId, Function.identity()));
		Map<Integer, UserCode> spcompCodes = userCodeRepository.findAllById(spcompidList)
				.stream().collect(Collectors.toMap(UserCode::getId, Function.identity()));
		Map<Integer, UserCode> spplanCodes = userCodeRepository.findAllById(spplanidList)
				.stream().collect(Collectors.toMap(UserCode::getId, Function.identity()));


		for(int i=0; i<spworkidList.size(); i++){

			String askseq = String.format("%03d", AskSeqInt);

			UserCode spworkid = spworkCodes.get(spworkidList.get(i));
			UserCode spcompid = spcompCodes.get(spcompidList.get(i));
			UserCode spplanid = spplanCodes.get(spplanidList.get(i));


			TB_RP945Dto rp945dto = TB_RP945Dto.builder()
					.userid(login_id)
					.askseq(askseq)
					.spworkcd(spworkid.getCode())
					.spcompcd(spcompid.getCode())
					.spplancd(spplanid.getCode())
					.spworknm(spworknmList.get(i))
					.spcompnm(spcompnmList.get(i))
					.spplannm(spplannmList.get(i))
					.spworkid(spworkid.getId())
					.spcompid(spcompid.getId())
					.spplanid(spplanid.getId())
					.build();
			tbRp945Service.save(rp945dto);

			AskSeqInt++;
		}
		result.success = true;
		result.message = "저장되었습니다.";

		return result;
	}
	/*@PostMapping("/save")
	@Transactional
	public AjaxResult saveUser(
			@RequestParam(value="id", required = false) Integer id,
			@RequestParam(value="Name") String Name,		//이름 (user_profile.Name)
			@RequestParam(value="login_id") String login_id, //사번 (auth_user.username)
			@RequestParam(value="email", required = false, defaultValue = "") String email,
			@RequestParam(value="Factory_id", required = false) Integer Factory_id,
			@RequestParam(value="Depart_id", required = false) Integer Depart_id,
			@RequestParam(value="UserGroup_id", required = false) Integer UserGroup_id,
			@RequestParam(value="lang_code", required = false) String lang_code,
			@RequestParam(value="is_active", required = false) Boolean is_active,
			HttpServletRequest request,
			Authentication auth
			) {

		AjaxResult result = new AjaxResult();

		String sql = null;
		User user = null;
		User loginUser = (User)auth.getPrincipal();
		Timestamp today = new Timestamp(System.currentTimeMillis());
		MapSqlParameterSource dicParam = new MapSqlParameterSource();
		boolean username_chk = this.userRepository.findByUsername(login_id).isEmpty();

		if(is_active == null) {
			is_active = false;
		}


		// new data일 경우
		if (id==null) {
			if (username_chk == false) {
				result.success = false;
				result.message="중복된 사번이 존재합니다.";
				return result;
			}
			user = new User();
			user.setPassword(Pbkdf2Sha256.encode("1"));
			user.setSuperUser(false);
			user.setLast_name("");
			user.setIs_staff(false);

			dicParam.addValue("loginUser", loginUser.getId());

			sql = """
		        	INSERT INTO user_profile
		        	("_created", "_creater_id", "User_id", "lang_code", "Name", "Factory_id" , "Depart_id", "UserGroup_id" )
		        	VALUES (now(), :loginUser, :User_id, :lang_code, :name, :Factory_id, :Depart_id, :UserGroup_id )
		        """;
		// 기존 user 수정일 경우
		} else {
			user = this.userRepository.getUserById(id);

			if (login_id.equals(user.getUsername())==false && username_chk == false) {
				result.success = false;
				result.message="중복된 사번이 존재합니다.";
				return result;
			}

			sql = """
					update user_profile set
					     	"lang_code" = :lang_code, "Name" = :name
					     	, "Factory_id" = :Factory_id, "Depart_id"= :Depart_id, "UserGroup_id" = :UserGroup_id
					where "User_id" = :User_id
		        """;

		}

        user.setUsername(login_id);
        user.setFirst_name(Name);
        user.setEmail(email);
        user.setDate_joined(today);
        user.setActive(is_active);

		user = this.userRepository.save(user);

		dicParam.addValue("name", Name);
		dicParam.addValue("UserGroup_id", UserGroup_id);
		dicParam.addValue("Factory_id", Factory_id);
		dicParam.addValue("Depart_id", Depart_id);
		dicParam.addValue("lang_code", lang_code);
        dicParam.addValue("User_id", user.getId());

        this.sqlRunner.execute(sql, dicParam);

		result.data = user;

		return result;
	}*/

	// user 삭제
	@Transactional
	@PostMapping("/delete")
	public AjaxResult deleteUser(@RequestParam("id") String id,
								 @RequestParam(value = "username", required = false) String username
	) {

		UtilClass util = new UtilClass();
		System.out.println(util.removeBrackers(username));
		Optional<User> user = userRepository.findByUsername(util.removeBrackers(username));

		if(user.isPresent()){
			tB_RP940Repository.deleteByUserid(user.get().getUsername());
			tB_RP945Repository.deleteByUserid(user.get().getUsername());
		}

		Integer userid  = Integer.parseInt(util.removeBrackers(id))  ;

		this.userRepository.deleteById(userid);
		AjaxResult result = new AjaxResult();
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
