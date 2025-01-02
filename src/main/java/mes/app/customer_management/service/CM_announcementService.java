package mes.app.customer_management.service;

import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CM_announcementService {
    @Autowired
    SqlRunner sqlRunner;

    // BBS read
    public List<Map<String, Object>> getBBSList() {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                SELECT
                    *
                FROM
                    TB_BBSINFO
                """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
        return items;
    }
    // BBS delete
    public List<Map<String, Object>> deleteBBS(int faqseq) {
        MapSqlParameterSource params = new MapSqlParameterSource();


        String sql = """
                DELETE FROM TB_BBSINFO
                WHERE FAQSEQ = :faqseq
                """;
        params.addValue("faqseq", faqseq);

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
        return items;
    }
    // File delete
    public List<Map<String, Object>> deleteFile(int faqseq) {
        MapSqlParameterSource params = new MapSqlParameterSource();


        String sql = """
                DELETE FROM TB_FILEINFO
                WHERE FAQSEQ = :faqseq
                """;
        params.addValue("faqseq", faqseq);

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
        return items;
    }
}
