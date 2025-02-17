package mes.app.CategoryManager.Service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.entity.UserCode;
import mes.domain.entity.actasEntity.TB_REGISTER;
import mes.domain.entity.factcheckEntity.TB_REGWORD;
import mes.domain.repository.UserCodeRepository;
import mes.domain.repository.actasRepository.TB_registerRepository;
import mes.domain.repository.factcheckRepository.TB_REGWORDRepository;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class TB_registerService {

    @Autowired
    private UserCodeRepository userCodeRepository;

    @Autowired
    TB_registerRepository tbRegisterRepository;

    @Autowired
    TB_REGWORDRepository tbRegwordRepository;

    @Autowired
    SqlRunner sqlRunner;

   @Transactional
   public TB_REGISTER saveOrUpdateRegister(Map<String, Object> formData) {
       Integer regSeq = formData.get("REGSEQ") != null ? Integer.parseInt(formData.get("REGSEQ").toString()) : null;

       TB_REGISTER tbRegister;
       if (regSeq != null) {
           tbRegister = tbRegisterRepository.findById(regSeq).orElseThrow(() ->
               new IllegalArgumentException("해당 ID의 데이터가 존재하지 않습니다."));
       } else {
           tbRegister = new TB_REGISTER();
       }

       if (formData.get("regnm") == null || formData.get("regnm").toString().isEmpty()) {
           throw new IllegalArgumentException("등기명칭은 필수 항목입니다.");
       }

       tbRegister.setRegNm(formData.get("regnm").toString());
       tbRegister.setRegGroup(formData.get("reggroup") != null ? formData.get("reggroup").toString() : null);
       tbRegister.setRegStand(formData.get("regstand") != null ? formData.get("regstand").toString() : null);
       tbRegister.setRegMaxNum(formData.get("regmaxnum") != null && !formData.get("regmaxnum").toString().trim().isEmpty()
           ? Integer.parseInt(formData.get("regmaxnum").toString().replace("점", "").trim())
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
       tbRegister.setRegAsName(formData.get("regAsName") != null ? formData.get("regAsName").toString() : null);
       tbRegister.setExFlag(formData.get("exflag") != null && formData.get("exflag").toString().equalsIgnoreCase("true") ? "1" : "0");
       tbRegister.setRegComment(formData.get("regComment") != null ? formData.get("regComment").toString() : null);
       tbRegister.setRemark(formData.get("remark") != null ? formData.get("remark").toString() : null);

       // 등록 정보 저장
       TB_REGISTER savedRegister = tbRegisterRepository.save(tbRegister);

       // 기존 키워드 삭제 (수정 시)
       if (regSeq != null) {
           tbRegwordRepository.deleteByRegSeq(savedRegister.getRegSeq());
       }

       // 키워드 저장 (USEYN = "Y")
       if (formData.get("keywords") instanceof List) {
           List<Map<String, String>> keywords = (List<Map<String, String>>) formData.get("keywords");

           for (Map<String, String> keywordMap : keywords) {
               String keyword = keywordMap.get("REGWORD");
               String keywordRemark = keywordMap.get("REGREMARK");

               if (keyword != null && !keyword.trim().isEmpty()) {
                   TB_REGWORD regword = TB_REGWORD.builder()
                       .regSeq(savedRegister.getRegSeq())  // 연결된 REGSEQ
                       .regWord(keyword)                  // 키워드
                       .regRemark(keywordRemark)          // 키워드 설명 추가
                       .useYn("Y")                        // 사용 여부
                       .build();

                   tbRegwordRepository.save(regword);
               }
           }
       }

       log.info("저장된 데이터: {}", savedRegister);
       return savedRegister;
   }

    // 숫자 필드 변환 ("점", "%" 제거)
    private BigDecimal parseNumericField(String value, String suffixToRemove) {
        log.info("Parsing numeric field: value={}, suffixToRemove={}", value, suffixToRemove);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // 접미사 제거 후 BigDecimal로 변환
            String sanitizedValue = value.replace(suffixToRemove, "").trim();
            return new BigDecimal(sanitizedValue);
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
            sql.append(" AND tr.regnm LIKE :searchInput");
            params.addValue("searchInput", "%" + searchInput + "%");
        }
        log.info("Generated SQL: {}", sql);
        log.info("SQL Parameters: {}", params.getValues());
        return sqlRunner.getRows(sql.toString(), params);
    }

    public List<Map<String, Object>> categorydetailsRead(String REGSEQ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
        SELECT
            tr.*,
            trg.REGREMARK,
            trg.REGWORD
        FROM TB_REGISTER tr
        LEFT JOIN TB_REGWORD trg ON trg.REGSEQ = tr.REGSEQ
        WHERE 1=1
    """);

        if (REGSEQ != null && !REGSEQ.isEmpty()) {
            sql.append(" AND tr.REGSEQ = :REGSEQ");
            params.addValue("REGSEQ", REGSEQ);
        }

        sql.append(" ORDER BY trg.REGNO");
        /*sql.append(" GROUP BY tr.REGSEQ, tr.regnm, tr.reggroup, tr.regstand, tr.regmaxnum, " +
            "tr.regyul, tr.regstamt, tr.riskclass, tr.subScore, tr.senScore, " +
            "tr.regAsName, tr.exflag, tr.regComment, tr.remark ," +
            "trg.REGREMARK,trg.REGWORD ");*/

        // SQL과 파라미터 로그 출력
        log.info("Generated SQL: {}", sql);
        log.info("SQL Parameters: {}", params.getValues());

        return sqlRunner.getRows(sql.toString(), params);
    }

    //삭제
    public void deleteRegisterById(Integer regSeq) {
        if (!tbRegisterRepository.existsById(regSeq)) {
            throw new IllegalArgumentException("해당 ID의 TB_REGISTER 데이터가 존재하지 않습니다.");
        }

        // TB_REGWORD 데이터가 존재하는지 확인
        if (tbRegwordRepository.existsById(regSeq)) {
            //TB_REGWORD 데이터 먼저 삭제
            tbRegwordRepository.deleteByRegSeq(regSeq);
        }

        // TB_REGISTER 데이터 삭제
        tbRegisterRepository.deleteById(regSeq);
    }
}