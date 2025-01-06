package mes.app.system;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mes.app.system.service.LoginLogService;
import mes.domain.model.AjaxResult;

@Slf4j
@RestController
@RequestMapping("/api/system/loginlog")
public class LoginLogController {

	@Autowired
	private LoginLogService loginLogService;

	// 로그인 로그 리스트 조회
	@GetMapping("/read")
	public AjaxResult getLoginLogList(
			@RequestParam(value="srchStartDt", required=false) String srchStartDt,
			@RequestParam(value="srchEndDt", required=false) String srchEndDt,
			@RequestParam(value="keyword", required=false) String keyword,
			@RequestParam(value="type", required=false) String type,
			HttpServletRequest request) {
		String start_date = srchStartDt + " 00:00:00";
		String end_date = srchEndDt + " 23:59:59";

		Timestamp start = Timestamp.valueOf(start_date);
		Timestamp end = Timestamp.valueOf(end_date);

		List<Map<String, Object>> items = this.loginLogService.getLoginLogList(start, end, keyword, type);

		AjaxResult result = new AjaxResult();
		result.data = items;

		return result;
	}

	@GetMapping("/read2")
	public AjaxResult getLoginLogList2(
			@RequestParam(value = "srchStartDt", required = false) String srchStartDt,
			@RequestParam(value = "srchEndDt", required = false) String srchEndDt,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "type", required = false) String type,
			HttpServletRequest request) {
		AjaxResult result = new AjaxResult();

		// 기본 날짜 설정
		String start_date = srchStartDt != null ? srchStartDt + " 00:00:00" : LocalDate.now().minusDays(7).toString() + " 00:00:00";
		String end_date = srchEndDt != null ? srchEndDt + " 23:59:59" : LocalDate.now().toString() + " 23:59:59";

		Timestamp start;
		Timestamp end;
		try {
			start = Timestamp.valueOf(start_date);
			end = Timestamp.valueOf(end_date);
		} catch (IllegalArgumentException e) {
			result.success = false;
			result.message = "날짜 형식이 잘못되었습니다. 올바른 형식: YYYY-MM-DD";
			return result;
		}

		// 데이터 조회
		List<Map<String, Object>> items;
		try {
			items = this.loginLogService.getLoginLogList2(start, end,
					keyword != null ? keyword.trim() : null,
					type != null ? type.trim() : null);
		} catch (Exception e) {
			result.success = false;
			result.message = "로그 데이터를 조회하는 동안 오류가 발생했습니다.";
			return result;
		}

		// 반환 전 날짜 포맷 처리
		if (items != null && !items.isEmpty()) {
			items.removeIf(item ->
					"Unknown".equals(item.get("name")) ||
							"Unknown".equals(item.get("login_id"))
			);

			LocalDate today = LocalDate.now();

			for (Map<String, Object> item : items) {
				// 상태 변경: 로그아웃 시간이 현재 날짜와 다른 경우
				if (item.containsKey("logout_time") && item.get("logout_time") != null) {
					try {
						String logoutTimeStr = item.get("logout_time").toString();
						LocalDate logoutDate = LocalDate.parse(logoutTimeStr.substring(0, 10)); // "YYYY-MM-DD" 추출
						if (!logoutDate.equals(today)) {
							item.put("state", "LOGOUT");
						}
					} catch (Exception e) {
						log.error("로그아웃 시간 처리 중 오류 발생: {}", e.getMessage());
					}
				}

				if (item.containsKey("useTime") && item.get("useTime") != null) {
					try {
						double useTime = Double.parseDouble(item.get("useTime").toString());
						int hours = (int) useTime; // 정수 부분은 시간
						int minutes = (int) Math.round((useTime - hours) * 60); // 소수 부분은 분으로 변환

						// "시간 분" 형식으로 변환하여 저장
						item.put("useTime", String.format("%d시간 %02d분", hours, minutes));
					} catch (NumberFormatException e) {
						// 숫자 변환 실패 시 기본값 또는 에러 처리
						item.put("useTime", "0시간 00분");
						log.error("useTime 값을 변환하는 동안 오류 발생: {}", e.getMessage());
					}
				}
			}
		}

		// 결과 반환
		if (items == null || items.isEmpty()) {
			result.success = true;
			result.data = Collections.emptyList();
			result.message = "조회된 데이터가 없습니다.";
		} else {
			result.success = true;
			result.data = items;
			result.message = "조회가 성공적으로 완료되었습니다.";
		}

		return result;
	}

}
