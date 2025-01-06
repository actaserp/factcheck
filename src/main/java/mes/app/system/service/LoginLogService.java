package mes.app.system.service;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.util.StringUtils;
import mes.domain.services.SqlRunner;

@Slf4j
@Service
public class LoginLogService {

    @Autowired
    SqlRunner sqlRunner;

    public List<Map<String, Object>> getLoginLogList(Timestamp start, Timestamp end, String keyword, String type) {

        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("start", start);
        dicParam.addValue("end", end);

        String sql = """
                select row_number() over (order by CONVERT(varchar, ll._created, 23) desc, up."Name" asc, ll._created desc) as row_number
                , ll.id
                , ll."Type" as type
                , ll."IPAddress" as addr
                , au.username as login_id
                , up."Name" as name
                , CONVERT(varchar, ll._created, 120) as created
                from login_log ll
                left join auth_user au ON au.id = ll."User_id" 
                left join user_profile up on up."User_id" = ll."User_id" 
                where ll._created between :start and :end
                """;

        // 'login', 'logout' 타입을 적용할 경우 필터 추가
        if (StringUtils.isNotEmpty(type)) {
            sql += " and ll.\"Type\" = :type ";
            dicParam.addValue("type", type);
        } else {
            sql += " and (ll.\"Type\" = 'login' or ll.\"Type\" = 'logout')";
        }

        // 키워드 검색 추가 조건
        if (StringUtils.isNotEmpty(keyword)) {
            sql += """ 
                    and (au.username LIKE '%' + :keyword + '%'
                        or up."Name" LIKE '%' + :keyword + '%' 
                    )
                    """;
            dicParam.addValue("keyword", keyword); // keyword가 있을 때만 파라미터 추가
        }

        // 정렬 조건은 항상 동일하게 적용
        sql += " order by" +
                "    CONVERT(varchar, ll._created, 23) desc," +
                "    up.\"Name\" asc, " +
                "    ll._created desc";
        System.out.println();
        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
        return items;
    }

    public List<Map<String, Object>> getLoginLogList2(Timestamp start, Timestamp end, String keyword, String type) {
        // 파라미터 설정
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("start", start);
        dicParam.addValue("end", end);

        // SQL 생성
        StringBuilder sql = new StringBuilder("""
        SELECT
             login_id AS name,
             first_name,
             날짜,
             email,
             MIN(첫_로그인_시간) AS login_time,
             MAX(마지막_로그아웃_시간) AS logout_time,
             CAST(SUM(일일_이용_시간_분) / 60.0 AS DECIMAL(10, 2)) AS useTime
        FROM (
            SELECT
                User_id,
                login_id,
                email,
                first_name,
                CONVERT(VARCHAR(10), Login_Time, 120) AS 날짜,
                Login_Time AS 첫_로그인_시간,
                CASE
                    WHEN CONVERT(VARCHAR(10), Login_Time, 120) = CONVERT(VARCHAR(10), Logout_Time, 120) THEN Logout_Time
                    ELSE DATEADD(SECOND, -1, DATEADD(DAY, 1, CONVERT(DATETIME, CONVERT(VARCHAR(10), Login_Time, 120))))
                END AS 마지막_로그아웃_시간,
                CASE
                    WHEN CONVERT(VARCHAR(10), Login_Time, 120) = CONVERT(VARCHAR(10), Logout_Time, 120) THEN DATEDIFF(MINUTE, Login_Time, Logout_Time)
                    ELSE DATEDIFF(MINUTE, Login_Time, DATEADD(SECOND, -1, DATEADD(DAY, 1, CONVERT(DATETIME, CONVERT(VARCHAR(10), Login_Time, 120)))))
                END AS 일일_이용_시간_분
            FROM (
                SELECT DISTINCT
                    ll.User_id AS User_id,
                    ll._created AS Login_Time,
                    lo._created AS Logout_Time,
                    au.username AS login_id,
                    au.email AS email,
                    au.first_name AS first_name,
                    ll."Type" AS login_type
                FROM
                    MOB_FACTCHK.dbo.login_log ll
                JOIN
                    MOB_FACTCHK.dbo.login_log lo
                    ON ll.User_id = lo.User_id
                    AND ll."Type" = 'login'
                    AND lo."Type" = 'logout'
                    AND lo._created > ll._created
                LEFT JOIN auth_user au ON au.id = ll."User_id"
                LEFT JOIN user_profile up ON up."User_id" = ll."User_id"
            ) AS SubQuery
        ) AS DailyUsage
        WHERE 날짜 BETWEEN :start AND :end
    """);

        // 키워드 검색 조건 추가
        if (StringUtils.isNotEmpty(keyword)) {
            sql.append("""
            AND (login_id LIKE '%' + :keyword + '%')
        """);
            dicParam.addValue("keyword", keyword);
        }

        // SQL 그룹화 및 정렬
        sql.append("""
        GROUP BY User_id, login_id, email,first_name, 날짜
        ORDER BY User_id, 날짜
    """);

        // SQL 실행
        try {
            log.info("Generated SQL: {}", sql);
            log.info("SQL Parameters: {}", dicParam.getValues());
            return this.sqlRunner.getRows(sql.toString(), dicParam);
        } catch (Exception e) {
            // 예외 처리
            System.err.println("SQL 실행 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("로그인 로그 조회 중 오류가 발생했습니다.", e);
        }
    }


}
