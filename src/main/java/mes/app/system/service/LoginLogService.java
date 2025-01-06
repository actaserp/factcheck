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
        WITH DailyUsage AS (
             SELECT
                 ll."User_id",
                 COALESCE(up."Name", 'Unknown') AS name,
                 COALESCE(au.username, 'Unknown') AS login_id,
                 au.email AS email,
                 CONVERT(VARCHAR(10), ll._created, 120) AS 날짜,
                 MIN(CASE WHEN ll."Type" = 'LOGIN' THEN ll._created ELSE NULL END) AS login_time,
                 MAX(CASE WHEN ll."Type" = 'LOGOUT' THEN ll._created ELSE NULL END) AS logout_time,
                 CASE
                     WHEN MAX(CASE WHEN ll."Type" = 'LOGOUT' THEN ll._created ELSE NULL END) IS NOT NULL
                          AND MAX(CASE WHEN ll."Type" = 'LOGOUT' THEN ll._created ELSE NULL END) <
                              MAX(CASE WHEN ll."Type" = 'LOGIN' THEN ll._created ELSE NULL END) THEN 'LOGIN'
                     WHEN MIN(CASE WHEN ll."Type" = 'LOGIN' THEN ll._created ELSE NULL END) IS NOT NULL
                          AND MAX(CASE WHEN ll."Type" = 'LOGOUT' THEN ll._created ELSE NULL END) IS NOT NULL THEN 'LOGOUT'
                     WHEN MIN(CASE WHEN ll."Type" = 'LOGIN' THEN ll._created ELSE NULL END) IS NOT NULL THEN 'LOGIN'
                     ELSE '로그아웃 중'
                 END AS state
             FROM
                 login_log ll
             LEFT JOIN user_profile up
                 ON up."User_id" = ll."User_id"
             LEFT JOIN auth_user au
                 ON au.id = ll."User_id"
             WHERE
                 ll._created BETWEEN :start AND :end
             GROUP BY
                 ll."User_id",
                 up."Name",
                 au.username,
                 au.email,
                 CONVERT(VARCHAR(10), ll._created, 120)
         ),
        UsageWithCounts AS (
            SELECT
                du.User_id,
                du.name,
                du.login_id,
                du.email,
                du.날짜,
                du.login_time,
                du.logout_time,
                du.state,
                COUNT(si.REQDATE) AS views -- TB_SEARCHINFO 테이블을 기반으로 조회 건수 계산
            FROM
                DailyUsage du
            LEFT JOIN MOB_FACTCHK.dbo.TB_SEARCHINFO si
                ON si.USERID = du.User_id
                AND si.REQDATE BETWEEN du.login_time AND du.logout_time
            GROUP BY
                du.User_id,
                du.name,
                du.login_id,
                du.email,
                du.날짜,
                du.login_time,
                du.logout_time,
                du.state
        )
        SELECT
            User_id,
            name,
            login_id,
            email,
            날짜,
            state,
            views,
            CONVERT(VARCHAR, login_time, 120) AS login_time,
            CASE
                WHEN logout_time IS NULL THEN '로그아웃 없음'
                ELSE CONVERT(VARCHAR, logout_time, 120)
            END AS logout_time,
            CASE
                WHEN logout_time IS NULL THEN NULL
                ELSE CAST(DATEDIFF(MINUTE, login_time, logout_time) / 60.0 AS DECIMAL(10, 2))
            END AS useTime
        FROM
            UsageWithCounts
        ORDER BY
            login_time DESC
    """);

        // 키워드 검색 조건 추가
        if (StringUtils.isNotEmpty(keyword)) {
            sql.insert(sql.indexOf("WHERE") + 5,
                    " (au.username LIKE '%' + :keyword + '%' OR up.Name LIKE '%' + :keyword + '%') AND");
            dicParam.addValue("keyword", keyword);
        }

        // SQL 실행
        try {
//            log.info("Generated SQL: \n{}", sql);
//            dicParam.getValues().forEach((key, value) -> log.info("SQL Parameter - {}: {}", key, value));
            return this.sqlRunner.getRows(sql.toString(), dicParam);
        } catch (Exception e) {
            log.error("SQL 실행 중 오류 발생", e);
            throw new RuntimeException("로그인 로그 조회 중 오류가 발생했습니다.", e);
        }
    }

}
