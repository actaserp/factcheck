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
    public List<Map<String, Object>> getBBSList(String searchText) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("searchText", "%" + searchText + "%");

        StringBuilder sql = new StringBuilder("""
                SELECT
                    b.*,
                    COALESCE((
                        SELECT JSON_QUERY((
                            SELECT
                                f.FILESEQ AS fileseq,
                                f.FILESIZE AS filesize,
                                f.FILEEXTNS AS fileextns,
                                f.FILEORNM AS fileornm,
                                f.FILEPATH AS filepath
                            FROM TB_FILEINFO f
                            WHERE b.BBSSEQ = f.BBSSEQ
                            AND f.CHECKSEQ = '01'
                            FOR JSON PATH
                        ))
                    ), '[]') AS fileInfos
                FROM
                    TB_BBSINFO b
                WHERE 1=1
                """);
        // 제목필터
        if (searchText != null && !searchText.isEmpty()) {
            sql.append(" AND b.BBSSUBJECT LIKE :searchText");
        }
        List<Map<String,Object>> items = this.sqlRunner.getRows(sql.toString(),params);
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
                AND FLAG = '01'
                """;
        params.addValue("faqseq", faqseq);

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
        return items;
    }
}
