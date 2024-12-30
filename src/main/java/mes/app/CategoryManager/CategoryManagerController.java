package mes.app.CategoryManager;

import lombok.extern.slf4j.Slf4j;
import mes.app.CategoryManager.Service.TB_registerService;
import mes.domain.entity.UserCode;
import mes.domain.entity.actasEntity.TB_REGISTER;
import mes.domain.model.AjaxResult;
import mes.domain.repository.UserCodeRepository;
import mes.domain.repository.actasRepository.TB_registerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/category")
public class CategoryManagerController {

    @Autowired
    TB_registerService tbRegisterService;

    @Autowired
    TB_registerRepository tbRegisterRepository;

    @Autowired
    private UserCodeRepository userCodeRepository;

    @GetMapping("/type")
    public ResponseEntity<Map<String, Object>> getCategoryTypes(
            @RequestParam(value = "parentCode", required = false) String parentCode) {
        // parentCode를 기준으로 하위 그룹 필터링
        List<UserCode> data = (parentCode != null)
                ? userCodeRepository.findByParentId(userCodeRepository.findByCode(parentCode).stream().findFirst().get().getId())
                : userCodeRepository.findAll();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "데이터 조회 성공");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/read")
    public ResponseEntity<Map<String, Object>> readCategories(@RequestParam(value = "regnm", required = false) String regnm) {
        List<TB_REGISTER> data;
        if (regnm != null && !regnm.isEmpty()) {
            data = tbRegisterRepository.findByRegNmContainingIgnoreCase(regnm); // 부분 검색
        } else {
            data = tbRegisterRepository.findAll();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveCategory(@RequestBody Map<String, Object> formData) {
        Map<String, Object> response = new HashMap<>();
        try {
            //log.info("받은 데이터: {}", formData);

            // 서비스 호출
            TB_REGISTER savedRegister = tbRegisterService.saveOrUpdateRegister(formData);

            // 성공 응답 구성
            response.put("success", true);
            response.put("message", "저장이 완료되었습니다.");
            response.put("data", savedRegister);
        } catch (IllegalArgumentException e) {
            // 유효성 검증 실패
            log.error("유효성 검증 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("저장 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "저장 중 오류가 발생했습니다. 관리자에게 문의하세요.");
        }
        return ResponseEntity.ok(response);
    }


}