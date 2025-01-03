package mes.app.GradeManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mes.app.GradeManager.service.TB_GRADEINFOService;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/GradeManager")
public class GradeManagerController {

    @Autowired
    TB_GRADEINFOService tb_gradeinfoService;

    @GetMapping("/read")
    public AjaxResult GradeRead(@RequestParam(value = "grflag", required = false) String grflag) {
        AjaxResult result = new AjaxResult();

        try {
            // 데이터 조회
            List<Map<String, Object>> GradeReadList = tb_gradeinfoService.getList(grflag);

            // 데이터가 있을 경우 성공 메시지
            result.success = true;
            result.message = "데이터 조회 성공";
            result.data = GradeReadList;
            log.info(result.toString());

        } catch (Exception e) {
            // 예외 처리
            result.success = false;
            result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
        }

        return result;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveGrade(@RequestBody Map<String, Object> formData) {
        Map<String, Object> response = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String prettyFormData = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(formData);
            log.info("Received Data:\n{}", prettyFormData);

            // 서비스에 데이터 저장 위임
            tb_gradeinfoService.saveGrade(formData);

            // 성공 응답 설정
            response.put("success", true);
            response.put("message", "저장이 완료되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("저장 중 오류 발생: {}", e.getMessage(), e);

            // 오류 응답 설정
            response.put("success", false);
            response.put("message", "데이터 저장 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/delete")
    public AjaxResult deleteCategory(@RequestBody Map<String, Object> requestData) {
        AjaxResult result = new AjaxResult();

        try {
            Object grid = requestData.get("grid");
            if (grid == null) {
                throw new IllegalArgumentException("삭제할 grid 값이 누락되었습니다.");
            }

            tb_gradeinfoService.deleteRegisterById(grid.toString());

            result.success = true;
            result.message = "삭제가 완료되었습니다.";
        } catch (Exception e) {
            result.success = false;
            result.message = "삭제 중 오류 발생: " + e.getMessage();
        }

        return result;
    }

}