package mes.app.user_management.service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MembershipVisitDataService {

    @Autowired
    SqlRunner sqlRunner;
    public List<Map<String, Object>> getList(String startDate, String endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
        SELECT
            FORMAT(INDATEM, 'yyyy-MM') AS RegisterMonth, -- 연월로 그룹화
            COUNT(*) AS RegisterCount -- 회원 수
        FROM
            TB_USERINFO
        WHERE
            INDATEM IS NOT NULL
    """);

        // 동적 조건 추가
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            sql.append(" AND INDATEM BETWEEN :startDate AND :endDate"); // 날짜 범위 조건
            params.addValue("startDate", startDate);
            params.addValue("endDate", endDate);
        }

        sql.append("""
        GROUP BY
            FORMAT(INDATEM, 'yyyy-MM')
        ORDER BY
            RegisterMonth
    """);

        //log.info("회원등록차트 SQL: {}", sql);
       // log.info("SQL Parameters: {}", params.getValues());

        // 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }

    public List<Map<String, Object>> getViewCountChart(String startDate, String endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""       
        SELECT
            FORMAT(CONVERT(DATETIME, REQDATE, 120), 'yyyy-MM') AS YearMonth, -- 연월로 그룹화
            COUNT(*) AS TotalInquiryCount -- 총 조회 건수
        FROM
            TB_SEARCHINFO se
        JOIN TB_USERINFO tu ON tu.USERID = se.USERID
        WHERE
            CONVERT(DATETIME, REQDATE, 120) BETWEEN :startDate AND :endDate
        """);
        // 동적 조건 추가
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            sql.append(" AND tu.INDATEM BETWEEN :startDate AND :endDate "); // 날짜 범위 조건
            params.addValue("startDate", startDate);
            params.addValue("endDate", endDate);
        }

        sql.append("""
        GROUP BY
           FORMAT(CONVERT(DATETIME, REQDATE, 120), 'yyyy-MM')
        ORDER BY
            YearMonth
        """);

        //log.info("조회건수List SQL: {}", sql);
        //log.info("SQL Parameters: {}", params.getValues());
        return sqlRunner.getRows(sql.toString(), params);
    }

    public List<Map<String, Object>> getGridList(String startDate, String endDate) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
       SELECT
            tu.USERID,
            tu.USERNM AS userNm,
            tu.USERADDR AS userAddr,
            tu.INDATEM as indatem,
            se.REQDATE as InquiryCount,
            COALESCE(req.InquiryCount, 0) AS InquiryCount
       FROM
            TB_USERINFO tu
       LEFT JOIN
            TB_SEARCHINFO se
            ON se.USERID = tu.USERID
       LEFT JOIN
            (SELECT
                USERID,
                COUNT(*) AS InquiryCount
             FROM
                TB_SEARCHINFO
             WHERE
                CONVERT(DATETIME, REQDATE, 120) BETWEEN :startDate AND :endDate
             GROUP BY
                USERID) req
            ON tu.USERID = req.USERID
       WHERE 1=1
    """);

        // 조건에 따라 동적으로 SQL 추가
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND tu.INDATEM >= :startDate");
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND tu.INDATEM <= :endDate");
            params.addValue("endDate", endDate);
        }

//        log.info("Generated SQL: {}", sql);
//        log.info("SQL Parameters: {}", params.getValues());
        // 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }
}
