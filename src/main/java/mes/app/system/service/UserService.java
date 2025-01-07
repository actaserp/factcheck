package mes.app.system.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import mes.app.account.service.TB_xuserService;
import mes.domain.entity.User;
import mes.domain.entity.UserGroup;
import mes.domain.entity.actasEntity.TB_XUSERS;
import mes.domain.repository.UserGroupRepository;
import mes.domain.repository.UserRepository;
import mes.domain.security.Pbkdf2Sha256;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import mes.domain.services.SqlRunner;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {

    @Autowired
    SqlRunner sqlRunner;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    TB_xuserService XusersService;
    @Autowired
    private UserGroupRepository userGroupRepository;

    // 사용자 리스트 조회
    /*public List<Map<String, Object>> getUserList(boolean superUser,String username, String email, String pernm, String userGroupId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("superUser", superUser);

        StringBuilder sql = new StringBuilder("""
        SELECT
            au.id,
            au.last_name,
            au.username AS userid,
            ug.id AS group_id,
            au.email,
            au.tel,
            au.agencycd,
            ug.Name AS group_name,
            au.last_login,
            up.lang_code,
            au.is_active,
            au.Phone,
            tx.pernm,
            FORMAT(au.date_joined, 'yyyy-MM-dd') AS date_joined
         FROM
             auth_user au
         LEFT JOIN
             user_profile up ON up.User_id = au.id
         LEFT JOIN
             user_group ug ON ug.id = up.UserGroup_id
         LEFT JOIN
             TB_XUSERS tx
             ON tx.pernm = au.first_name
         WHERE
             1 = 1
        """);
        // 사용자 이름 조건
        if (username != null && !username.trim().isEmpty()) {
            sql.append(" AND au.username LIKE :username");
            params.addValue("username", "%" + username.trim() + "%");
        }

        // 이름 조건
        if (pernm != null && !pernm.trim().isEmpty()) {
            sql.append(" AND au.first_name LIKE :pernm");
            params.addValue("pernm", "%" + pernm.trim() + "%");
        }

        // 사용자 그룹 조건
        if (userGroupId != null && !userGroupId.trim().isEmpty()) {
            sql.append(" AND up.UserGrup_id = :userGroupId");
            params.addValue("userGroupId", userGroupId);
        }

        // 이메일 조건
        if (email != null && !email.trim().isEmpty()) {
            sql.append(" AND au.email LIKE :email");
            params.addValue("email", "%" + email.trim() + "%");
        }

        // 정렬 조건 추가
        sql.append(" ORDER BY au.date_joined DESC");

        // SQL 디버깅 로그
        log.info("Executing SQL: {} with Parameters: {}", sql, params.getValues());

        // SQL 실행 후 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }*/
    public List<Map<String, Object>> getUserList(boolean superUser, String username, String email, String pernm, String userGroupId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("superUser", superUser);

        StringBuilder sql = new StringBuilder("""
    SELECT
         au.id,
         au.last_name,
         au.username AS userid,
         ug.id AS group_id,
         au.email,
         au.tel,
         au.agencycd,
         ug.Name AS group_name,
         au.last_login,
         up.lang_code,
         au.is_active,
         au.Phone,
         tx.pernm,
         FORMAT(au.date_joined, 'yyyy-MM-dd') AS date_joined
     FROM
         auth_user au
     LEFT JOIN
         user_profile up ON up.User_id = au.id
     LEFT JOIN
         user_group ug ON ug.id = up.UserGroup_id
     LEFT JOIN
         TB_XUSERS tx ON tx.pernm = au.first_name
     WHERE
         1 = 1
    """);

        if (username != null && !username.trim().isEmpty()) {
            sql.append(" AND LOWER(au.username) LIKE LOWER(:username)");
            params.addValue("username", "%" + username.trim().toLowerCase() + "%");
        }

        if (pernm != null && !pernm.trim().isEmpty()) {
            sql.append(" AND LOWER(au.first_name) LIKE LOWER(:pernm)");
            params.addValue("pernm", "%" + pernm.trim().toLowerCase() + "%");
        }

        if (userGroupId != null && !userGroupId.trim().isEmpty()) {
            sql.append(" AND up.UserGroup_id = :userGroupId");
            params.addValue("userGroupId", Integer.parseInt(userGroupId.trim()));
        }

        if (email != null && !email.trim().isEmpty()) {
            sql.append(" AND LOWER(au.email) LIKE LOWER(:email)");
            params.addValue("email", "%" + email.trim().toLowerCase() + "%");
        }

        sql.append(" ORDER BY au.date_joined DESC");

//        log.info("Generated SQL: {}", sql.toString());
//        log.info("SQL Parameters: {}", params.getValues());

        return sqlRunner.getRows(sql.toString(), params);
    }


    // 사용자 상세정보 조회
    public Map<String, Object> getUserDetail(Integer id){

        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("id", id);

        String sql = """
			select au.id
              , up."Name"
              , au.username as login_id
              , au.email
              , ug."Name" as group_name
              , up."UserGroup_id"
              , up."Factory_id"
              , f."Name" as factory_name
              , d."Name" as dept_name
              , up."Depart_id"
              , up.lang_code
              , au.is_active
              , to_char(au.date_joined ,'yyyy-mm-dd hh24:mi') as date_joined
            from auth_user au 
            left join user_profile up on up."User_id" = au.id
            left join user_group ug on up."UserGroup_id" = ug.id 
            left join factory f on up."Factory_id" = f.id 
            left join depart d on d.id = up."Depart_id"
            where au.id = :id
		    """;

        Map<String, Object> item = this.sqlRunner.getRow(sql, dicParam);

        return item;
    }

    // 사용자 그룹 조회
    public List<Map<String, Object>> getUserGrpList(Integer id) {

        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("id", id);

        String sql = """
        		select ug.id as grp_id
	            , ug."Name" as grp_name
	            ,rd."Char1" as grp_check
	            from user_group ug
	            left join rela_data rd on rd."DataPk2" = ug.id 
	            and "RelationName" = 'auth_user-user_group' 
	            and rd."DataPk1" = :id
	            where coalesce(ug."Code",'') <> 'dev'
        		""";

        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
        return items;
    }

    public Boolean SaveUser(User user, Authentication auth, String authType, String authCode){

        try {
            User UserEntity = userRepository.save(user);

            List<UserGroup> AuthGroup = userGroupRepository.findByCodeAndName(authType, authCode);


            MapSqlParameterSource dicParam = new MapSqlParameterSource();


            User loginUser = (User) auth.getPrincipal();


            dicParam.addValue("loginUser", loginUser.getId());

            String sql = """
		        	INSERT INTO user_profile 
		        	("_created", "_creater_id", "User_id", "lang_code", "Name", "UserGroup_id" ) 
		        	VALUES (now(), :loginUser, :User_id, :lang_code, :name, :UserGroup_id )
		        """;

            dicParam.addValue("name", user.getFirst_name());
            dicParam.addValue("lang_code", "ko-KR");
            //dicParam.addValue("UserGroup_id", );
            dicParam.addValue("User_id", UserEntity.getId());
            dicParam.addValue("lang_code", "ko-KR");
            dicParam.addValue("UserGroup_id", AuthGroup.get(0).getId());

            this.sqlRunner.execute(sql, dicParam);
            return true;
        }catch(Exception e){
            e.getMessage();
            return false;
        }


    }


    public List<Map<String, Object>> getUserSandanList(String id) {

        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("id", id);

        String sql = """
                SELECT t.userid,
                       t.spcompid AS spcompcd,
                       u_c."Value" AS spcompnm,
                       t.spplanid AS spplancd,
                       u_p."Value" AS spplannm,
                       t.spworkid AS spworkcd,
                       u_w."Value" AS spworknm,
                       t.askseq
                FROM tb_rp945 AS t
                LEFT JOIN user_code u_c ON t.spcompid = u_c.id
                LEFT JOIN user_code u_p ON t.spplanid = u_p.id
                LEFT JOIN user_code u_w ON t.spworkid = u_w.id
                WHERE t.userid = :id
                ORDER BY t.askseq;
                """;

        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
        return items;
    }


    public Map<String, Object> getUserDetailById(String id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        String sql = """
        SELECT
            au.id ,
            au.last_name,
            au.username AS userid,
            au.email,
            au.tel,
            au.agencycd,
            au.last_login,
            au.is_active,
            au.Phone,
            FORMAT(au.date_joined, 'yyyy-MM-dd') AS date_joined,
            ug.id AS group_id,
            ug.Name AS group_name,
            up.lang_code ,
            au.*
        FROM
            auth_user au
        LEFT JOIN
            user_profile up ON up.User_id = au.id
        LEFT JOIN
            user_group ug ON ug.id = up.UserGroup_id
        LEFT JOIN
             TB_XUSERS tx ON tx.pernm = au.first_name
        WHERE
            au.id = :id
            """;
        // SQL 디버깅 로그
        log.info("Executing SQL:\n{}\nWith Parameters: {}", sql, params.getValues());
        // SQL 디버깅 로그
        log.info("Executing SQL: {} with params: {}", sql, params.getValues());
        return sqlRunner.getRow(sql, params);
    }

    public List<Map<String, Object>> getCustcdAndSpjangcd(String spjangcd) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("spjangcd", spjangcd);

        String sql = """
                select custcd, spjangcd 
                from tb_xa012 
                where spjangcd = :spjangcd
                """;

        return sqlRunner.getRows(sql, params);
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void updateUserInfo(Map<String, Object> requestData) {
        Integer id = requestData.get("id") != null ? Integer.valueOf(requestData.get("id").toString()) : null;
        String name = (String) requestData.get("name");
        String password = (String) requestData.get("password");
        String phone = (String) requestData.get("phone");
        String email = (String) requestData.get("email");

        if (id == null) {
            throw new IllegalArgumentException("사용자 ID가 제공되지 않았습니다.");
        }

        // 사용자 조회
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.error("사용자 조회 실패: id={}", id);
            return new RuntimeException("해당 사용자가 없습니다.");
        });

        // 비밀번호 암호화
        String encodedPassword = password != null && !password.isEmpty()
                ? Pbkdf2Sha256.encode(password)
                : user.getPassword();

        // 사용자 정보 수정
        user.setPassword(encodedPassword);
        if (email != null) user.setEmail(email);
        if (name != null) {
            user.setFirst_name(name);
            user.setLast_name(name);
        }
        if (phone != null) user.setPhone(phone);

        // 사용자 정보 저장
        userRepository.save(user);

        log.info("사용자 정보 저장 완료: {}", user);

        // TB_XUSERS 업데이트
        TB_XUSERS xusers = TB_XUSERS.builder()
                .passwd1(encodedPassword)
                .passwd2(encodedPassword)
                .shapass(encodedPassword)
                .pernm(name)
                .build();

        XusersService.save(xusers);
    }

    public boolean existsById(Integer id) {
        if (id == null) {
            return false;
        }
        return userRepository.existsById(id);
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

}
