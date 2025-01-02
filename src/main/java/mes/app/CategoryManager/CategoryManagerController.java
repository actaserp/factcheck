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
    private UserCodeRepository userCodeRepository;

    @GetMapping("/type")
    public AjaxResult getCategoryTypes(
            @RequestParam(value = "parentCode", required = false) String parentCode) {
        AjaxResult result = new AjaxResult();

        try {
            // parentCode를 기준으로 하위 그룹 필터링
            List<UserCode> data = (parentCode != null)
                    ? userCodeRepository.findByParentId(userCodeRepository.findByCode(parentCode).stream().findFirst().get().getId())
                    : userCodeRepository.findAll();

            // 성공 시 데이터와 메시지 설정
            result.success = true;
            result.message = "데이터 조회 성공";
            result.data = data;

        } catch (Exception e) {
            // 예외 발생 시 처리
            result.success = false;
            result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
        }

        return result;
    }


    @GetMapping("/read")
    public AjaxResult categoryRead(@RequestParam(value = "regnm", required = false) String regnm) {
        AjaxResult result = new AjaxResult();

        try {
            // 데이터 조회
            List<Map<String, Object>> categoryList = tbRegisterService.getList(regnm);

            // 데이터가 있을 경우 성공 메시지
            result.success = true;
            result.message = "데이터 조회 성공";
            result.data = categoryList;

        } catch (Exception e) {
            // 예외 처리
            result.success = false;
            result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
        }

        return result;
    }

    @GetMapping("/details")
    public AjaxResult categorydetailsRead(@RequestParam(value = "REGSEQ", required = false) String REGSEQ) {
        AjaxResult result = new AjaxResult();
        try {
            // 데이터 조회
            List<Map<String, Object>> categorydetailsRead = tbRegisterService.categorydetailsRead(REGSEQ);

            if (categorydetailsRead != null && !categorydetailsRead.isEmpty()) {
                // 데이터가 있을 경우 첫 번째 데이터를 반환
                result.success = true;
                result.message = "데이터 조회 성공";
                result.data = categorydetailsRead.get(0); // 첫 번째 데이터 반환
            } else {
                // 데이터가 없을 경우 메시지 설정
                result.success = false;
                result.message = "조회된 데이터가 없습니다.";
            }
        } catch (Exception e) {
            // 예외 처리
            result.success = false;
            result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
        }

        return result;
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

    @PostMapping("/delete")
    public AjaxResult deleteCategory(@RequestBody Map<String, Object> requestData) {
        AjaxResult result = new AjaxResult();

        try {
            String regSeqStr = (String) requestData.get("REGSEQ");
            if (regSeqStr == null || regSeqStr.isEmpty()) {
                throw new IllegalArgumentException("삭제할 데이터의 REGSEQ가 누락되었습니다.");
            }

            Integer regSeq = Integer.parseInt(regSeqStr);
            tbRegisterService.deleteRegisterById(regSeq);

            result.success = true;
            result.message = "삭제가 완료되었습니다.";
        } catch (Exception e) {
            result.success = false;
            result.message = "삭제 중 오류 발생: " + e.getMessage();
        }

        return result;
    }

}