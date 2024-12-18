package mes.app.system;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mes.app.system.service.SystemService;
import mes.domain.entity.User;
import mes.domain.model.AjaxResult;
import mes.domain.services.CommonUtil;
import mes.domain.services.SqlRunner;

@RestController
@RequestMapping("/api/system/usergroupmenu")
public class UserGroupMenuController {

	@Autowired
	SystemService systemService;
	
	@Autowired
	SqlRunner sqlRunner;


	@GetMapping("/read")
	public AjaxResult getUserGroupMenuList(
			@RequestParam(value="group_id", required = false) Integer groupId,
			@RequestParam(value="folder_id", required = false) Integer folderId
	) {

		AjaxResult result = new AjaxResult();
		result.data = this.systemService.getUserGroupMenuList(groupId, folderId);
		return result;
	}

	@PostMapping("/save")
	@Transactional
	public AjaxResult saveUserGroupMenus(
			@RequestParam(value = "group_id") Integer groupId,
			@RequestParam(value = "Q") String strUserGroupMenuList,
			Authentication auth
	) {
		//System.out.println("Received User Group Menu List: " + strUserGroupMenuList);
		AjaxResult result = new AjaxResult();
		User user = (User) auth.getPrincipal();

		try {
			// JSON 데이터 파싱
			List<Map<String, Object>> userGroupMenuList = CommonUtil.loadJsonListMap(strUserGroupMenuList);

			userGroupMenuList.forEach(map -> {
				String menuCode = (String) map.get("menu_code");
				Integer ugm_id = (Integer) map.get("ugm_id");

				boolean r = Boolean.parseBoolean(map.get("r").toString());
				boolean w = Boolean.parseBoolean(map.get("w").toString());

				// 권한 코드 생성
				String authCode = "";
				if(r) {
					authCode="R";
				}
				if(w) {
					authCode+="W";
				}

				// SQL 매개변수 설정
				MapSqlParameterSource dicParam = new MapSqlParameterSource();
				dicParam.addValue("auth_code", authCode);
				dicParam.addValue("menu_code", menuCode);
				dicParam.addValue("group_id", groupId);
				dicParam.addValue("user_id", user.getId());

				String sql;

				if (ugm_id == null) {
					// INSERT 구문
					sql = """
                INSERT INTO user_group_menu (UserGroup_id, MenuCode, AuthCode, _creater_id, _created)
                VALUES (:group_id, :menu_code, :auth_code, :user_id, GETDATE())
                """;
				} else {
					// UPDATE 구문
					dicParam.addValue("id", ugm_id);
					sql = """
                UPDATE user_group_menu
                SET AuthCode = :auth_code,
                    UserGroup_id = :group_id,
                    MenuCode = :menu_code,
                    _modifier_id = :user_id,
                    _modified = GETDATE()
                WHERE id = :id
                """;
				}

				try {
					this.sqlRunner.execute(sql, dicParam);
				} catch (Exception e) {
					throw new RuntimeException("SQL 실행 중 오류 발생: menu_code=" + menuCode, e);
				}
			});

			// 성공 메시지 반환
			result.success = true;
			result.message = "저장이 완료되었습니다.";
		} catch (Exception e) {
			System.err.println("Error during saveUserGroupMenus: " + e.getMessage());
			result.success = false;
			result.message = "저장 중 오류가 발생했습니다. 관리자에게 문의하세요.";
		}
		return result;
	}
}

