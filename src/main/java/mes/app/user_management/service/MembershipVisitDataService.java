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
            FORMAT(REQDATE, 'yyyy-MM') AS YearMonth, -- 연월
            COUNT(*) AS TotalInquiryCount -- 총 조회 건수
        FROM TB_SEARCHINFO
        WHERE 1 = 1
        """);

        // 동적 조건 추가
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            sql.append(" AND REQDATE >= :startDate AND REQDATE <= :endDate ");
            params.addValue("startDate", startDate);
            params.addValue("endDate", endDate);
        }

        sql.append(" GROUP BY FORMAT(REQDATE, 'yyyy-MM') ORDER BY YearMonth;"); // 연월별 그룹화 및 정렬

        //log.info("조회건수List SQL: {}", sql);
        //log.info("SQL Parameters: {}", params.getValues());

        return sqlRunner.getRows(sql.toString(), params);
    }



    public List<Map<String, Object>> getGridList(String startDate, String endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        // SQL 기본 쿼리
        StringBuilder sql = new StringBuilder("""
        SELECT
        TU.USERID,
         COUNT(*) AS InquiryCount, 
         TU.usernm as userNm,
         TU.USERADDR as userAddr,
         TU.INDATEM as indatem
            FROM TB_REALINFO RI
            JOIN TB_USERINFO TU ON TU.userid = RI.USERID\s
            WHERE 1=1
        """);

        // 동적 조건 추가
        if (startDate != null && !startDate.isEmpty()) {
            sql.append("AND RI.RELASTDATE >= :startDate ");
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND RI.RELASTDATE <= :endDate ");
            params.addValue("endDate", endDate);
        }

        // 그룹화 및 정렬 추가
        sql.append("""
           GROUP BY TU.userid, TU.usernm, TU.INDATEM, TU.USERADDR
           ORDER BY InquiryCount DESC
        """);

        // 로깅 활성화
        log.info("회원등록 현황 그리드 SQL: {}", sql);
        log.info("SQL Parameters: {}", params.getValues());

        // 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }


    //조회 건수
    public List<Map<String, Object>> getGridList2(String startDate, String endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
        SELECT
            tu.USERID,
            tu.USERNM AS userNm,
            ri.REALADD AS userAddr,
            ri.REQDATE AS issueDate,
            COUNT(ri.REQDATE) AS TotalCount
        FROM TB_USERINFO tu
        LEFT JOIN TB_REALINFO ri
            ON tu.USERID = ri.USERID
        WHERE 1 = 1
    """);

        // 동적 조건 추가
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND ri.REQDATE >= :startDate ");
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND ri.REQDATE <= :endDate ");
            params.addValue("endDate", endDate);
        }

        // GROUP BY 및 ORDER BY 추가
        sql.append("""
        GROUP BY
            tu.USERID,
            tu.USERNM,
            ri.REALADD,
            ri.REQDATE
        ORDER BY ri.REQDATE, tu.USERID
    """);

        // SQL 디버깅용 로그 (필요시 활성화)
         //log.info("발급 건수 현황 그리드 SQL: {}", sql);
         //log.info("SQL Parameters: {}", params.getValues());

        // 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }
}
