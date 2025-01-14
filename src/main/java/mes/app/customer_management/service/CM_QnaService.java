package mes.app.customer_management.service;

import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CM_QnaService {
    @Autowired
    SqlRunner sqlRunner;

    // QnA 문의내역 리스트
    public List<Map<String, Object>> getQnAList(String searchText) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        StringBuilder sql = new StringBuilder("""
                   SELECT
                        Q.QSTSEQ,
                        Q.QSTTEXT,
                        Q.FLAG,
                        Q.QSTSORT,
                        Q.CHSEQ,
                        Q.INDATEM,
                        Q.INUSERID,
                        Q.USERID,
                        Q.INDATEM,
                        A.QSTSEQ AS answerNo,
                        A.QSTTEXT AS answer,
                        A.INDATEM AS answerdate,
                        A.QSTTEL AS answerTel,
                        STRING_AGG(CAST(F.FILEORNM AS VARCHAR(MAX)), ',') AS fileornm,
                        STRING_AGG(CAST(F.FILEPATH AS VARCHAR(MAX)), ',') AS filepath,
                        STRING_AGG(CAST(F.FILESIZE AS VARCHAR(MAX)), ',') AS filesize,
                        STRING_AGG(CAST(F.FILESVNM AS VARCHAR(MAX)), ',') AS filesvnm,
                        STRING_AGG(CAST(F.FILEEXTNS AS VARCHAR(MAX)), ',') AS fileextns
                    FROM
                        TB_USERQST Q
                    LEFT JOIN
                        TB_USERQST A
                    ON
                        Q.QSTSEQ = A.CHSEQ
                    LEFT JOIN
                        TB_FILEINFO F
                    ON
                        A.QSTSEQ = F.BBSSEQ
                        AND F.CHECKSEQ = '02'
                    WHERE
                        Q.FLAG = '0'
                """);
        // 문의내용필터
        if (searchText != null && !searchText.isEmpty()) {
            sql.append(" AND Q.USERID LIKE :searchText");
            params.addValue("searchText", "%" + searchText + "%");
        }
        //  GROUP BY 조건 추가
        sql.append(" GROUP BY" +
                "                    Q.QSTSEQ, Q.INDATEM, Q.USERID, Q.QSTTEXT, Q.FLAG, Q.QSTSORT, Q.CHSEQ, Q.INDATEM, Q.INUSERID, A.QSTSEQ, A.QSTTEXT, A.INDATEM, A.QSTTEL");
        // 정렬 조건 추가
        sql.append(" ORDER BY Q.INDATEM DESC");
        List<Map<String,Object>> items = this.sqlRunner.getRows(sql.toString(),params);
        return items;
    }
    // QnA delete
    public void deleteQnA(int qstseq) {
        MapSqlParameterSource params = new MapSqlParameterSource();


        String sql = """
                DELETE FROM TB_USERQST
                WHERE QSTSEQ = :qstseq
                """;
        params.addValue("qstseq", qstseq);

        int deleteCnt = this.sqlRunner.execute(sql,params);
    }
    // File delete
    public void deleteFile(int qstseq) {
        MapSqlParameterSource params = new MapSqlParameterSource();


        String sql = """
                DELETE FROM TB_FILEINFO
                WHERE bbsseq = :qstseq
                AND CHECKSEQ = '02'
                """;
        params.addValue("qstseq", qstseq);

        int deleteCnt = this.sqlRunner.execute(sql,params);
    }
}
