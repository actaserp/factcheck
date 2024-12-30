package mes.app.CategoryManager.Service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.entity.UserCode;
import mes.domain.entity.actasEntity.TB_REGISTER;
import mes.domain.repository.UserCodeRepository;
import mes.domain.repository.actasRepository.TB_registerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class TB_registerService {

    @Autowired
    private UserCodeRepository userCodeRepository;
    @Autowired
    TB_registerRepository tbRegisterRepository;

    public TB_REGISTER saveOrUpdateRegister(Map<String, Object> formData) {
        // 기본 키 확인 (수정인지 새로 저장인지 판단)
        Integer regSeq = formData.get("regSeq") != null ? Integer.parseInt(formData.get("regSeq").toString()) : null;

        // 기존 엔티티 조회 또는 새로 생성
        TB_REGISTER tbRegister = (regSeq != null)
                ? tbRegisterRepository.findById(regSeq).orElse(new TB_REGISTER())
                : new TB_REGISTER();

        // 필수 값 확인
        if (formData.get("regnm") == null || formData.get("regnm").toString().isEmpty()) {
            throw new IllegalArgumentException("등기명칭은 필수 항목입니다.");
        }

        // 데이터 매핑
        tbRegister.setRegNm(formData.get("regnm").toString());
        tbRegister.setRegGroup(formData.get("reggroup") != null ? formData.get("reggroup").toString() : null);
        tbRegister.setRegStand(formData.get("regstand") != null ? formData.get("regstand").toString() : null);

        // 숫자 및 금액 필드 처리
        tbRegister.setRegMaxNum(formData.get("regmaxnum") != null
                ? parseNumericField(formData.get("regmaxnum").toString(), "점")
                : null);

        tbRegister.setRegYul(formData.get("regyul") != null
                ? parseNumericField(formData.get("regyul").toString(), "%")
                : null);

        tbRegister.setRegStAmt(formData.get("regstamt") != null
                ? parseFloatField(formData.get("regstamt").toString(), "만원")
                : null);

        tbRegister.setRiskClass(formData.get("riskclass") != null ? formData.get("riskclass").toString() : null);
        tbRegister.setSubScore(formData.get("SUBSCORE") != null ? Integer.parseInt(formData.get("SUBSCORE").toString()) : null);
        tbRegister.setSenScore(formData.get("SENSCORE") != null ? Integer.parseInt(formData.get("SENSCORE").toString()) : null);
        tbRegister.setRegAsName(formData.get("normalizedName") != null ? formData.get("normalizedName").toString() : null);

        // boolean 처리
        tbRegister.setExFlag(formData.get("exflag") != null && formData.get("exflag").toString().equalsIgnoreCase("true") ? "1" : "0");

        tbRegister.setRegComment(formData.get("regcomment") != null ? formData.get("regcomment").toString() : null);
        tbRegister.setRemark(formData.get("remark") != null ? formData.get("remark").toString() : null);

        // 처리된 데이터 로그
        //log.info("처리된 TB_REGISTER 데이터: {}", tbRegister);

        // 저장
        return tbRegisterRepository.save(tbRegister);
    }


    // 숫자 필드 변환 ("점", "%" 제거)
    private Integer parseNumericField(String value, String suffixToRemove) {
        //log.info("Parsing numeric field: value={}, suffixToRemove={}", value, suffixToRemove);
        try {
            return Integer.parseInt(value.replace(suffixToRemove, "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자 형식 변환 중 오류 발생: " + value, e);
        }
    }

    private Float parseFloatField(String value, String suffixToRemove) {
        //log.info("Parsing float field: value={}, suffixToRemove={}", value, suffixToRemove);
        try {
            return Float.parseFloat(value.replaceAll("[^0-9.]", "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("금액 형식 변환 중 오류 발생: " + value, e);
        }
    }

}