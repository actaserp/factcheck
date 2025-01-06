package mes.app.CategoryManager.Service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.entity.UserCode;
import mes.domain.entity.actasEntity.TB_REGISTER;
import mes.domain.repository.UserCodeRepository;
import mes.domain.repository.actasRepository.TB_registerRepository;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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

    @Autowired
    SqlRunner sqlRunner;

    public TB_REGISTER saveOrUpdateRegister(Map<String, Object> formData) {
        //수정인지 새로 저장인지 확인
        Integer regSeq = formData.get("REGSEQ") != null ? Integer.parseInt(formData.get("REGSEQ").toString()) : null;

        TB_REGISTER tbRegister;
        if (regSeq != null) {
            //log.info("수정 로직 실행");
            tbRegister = tbRegisterRepository.findById(regSeq).orElseThrow(() ->
                    new IllegalArgumentException("해당 ID의 데이터가 존재하지 않습니다.")
            );
        } else {
            //log.info("신규 등록 로직 실행");
            tbRegister = new TB_REGISTER();
        }

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

        if (formData.get("subScore") != null && !formData.get("subScore").toString().isEmpty()) {
            tbRegister.setSubScore(Integer.parseInt(formData.get("subScore").toString()));
        } else {
            tbRegister.setSubScore(null);
        }

        if (formData.get("senScore") != null && !formData.get("senScore").toString().isEmpty()) {
            tbRegister.setSenScore(Integer.parseInt(formData.get("senScore").toString()));
        } else {
            tbRegister.setSenScore(null);
        }
        tbRegister.setRegAsName(formData.get("regAsName") != null ? formData.get("regAsName").toString() : null);

        // boolean 처리
        tbRegister.setExFlag(formData.get("exflag") != null && formData.get("exflag").toString().equalsIgnoreCase("true") ? "1" : "0");

        tbRegister.setRegComment(formData.get("regComment") != null ? formData.get("regComment").toString() : null);
        tbRegister.setRemark(formData.get("remark") != null ? formData.get("remark").toString() : null);

        // 저장
        //log.info("저장된 데이터: {}", tbRegister);
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

    public List<Map<String, Object>> getList(String searchInput) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
     SELECT
         tr.REGSEQ,
         tr.regnm,
         uc2.Value AS reggroup_display,
         tr.reggroup,
         tr.regmaxnum,
         tr.regstamt,
         tr.exflag,
         uc3.Value AS regStand_display,
         tr.regStand,
         tr.*
     FROM TB_REGISTER tr
     JOIN user_code uc2
         ON uc2.Code = tr.reggroup
     JOIN user_code uc3
         ON uc3.Code = tr.regStand
     WHERE 1=1
    """);

        if (searchInput != null && !searchInput.isEmpty()) {
            sql.append(" AND uc1.Value LIKE :regnm_display");
            params.addValue("regnm_display", "%" + searchInput + "%");
        }
        //log.info("Generated SQL: {}", sql);
        //log.info("SQL Parameters: {}", params.getValues());
        return sqlRunner.getRows(sql.toString(), params);
    }


    public List<Map<String, Object>> categorydetailsRead(String REGSEQ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
            SELECT
                tr.*
            FROM TB_REGISTER tr
            WHERE 1=1
        """);

        if (REGSEQ != null && !REGSEQ.isEmpty()) {
            sql.append(" AND tr.REGSEQ = :REGSEQ");
            params.addValue("REGSEQ", REGSEQ);
        }

        // SQL과 파라미터 로그 출력
       // log.info("Generated SQL: {}", sql);
       // log.info("SQL Parameters: {}", params.getValues());
        return sqlRunner.getRows(sql.toString(), params);
    }

    public void deleteRegisterById(Integer regSeq) {
        if (!tbRegisterRepository.existsById(regSeq)) {
            throw new IllegalArgumentException("해당 ID의 데이터가 존재하지 않습니다.");
        }
        tbRegisterRepository.deleteById(regSeq);
    }
}