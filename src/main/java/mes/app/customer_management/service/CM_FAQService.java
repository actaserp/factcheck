package mes.app.customer_management.service;

import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CM_FAQService {

    @Autowired
    SqlRunner sqlRunner;

    // FAQ 문의내역 리스트
    public List<Map<String, Object>> getFAQList() {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                SELECT
                    Q.FAQSEQ,
                    Q.FAQTEXT,
                    Q.FAQFLAG,
                    Q.FASORT,
                    Q.FLAG,
                    Q.INDATEM,
                    Q.INUSERID,
                    A.REMARK,
                    A.FAQSEQ AS answerNo,
                    A.FAQTEXT AS answer
                FROM
                    MOB_FACTCHK.dbo.TB_FAQINFO Q
                LEFT JOIN
                    MOB_FACTCHK.dbo.TB_FAQINFO A
                ON
                    Q.FAQSEQ = A.CHSEQ
                WHERE
                    Q.FLAG = '0'
                
                """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
        return items;
    }

    // FAQ delete
    public List<Map<String, Object>> deleteFAQ(int faqseq) {
        MapSqlParameterSource params = new MapSqlParameterSource();


        String sql = """
                DELETE FROM TB_FAQINFO
                WHERE FAQSEQ = :faqseq
                """;
        params.addValue("faqseq", faqseq);

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
        return items;
    }
}
