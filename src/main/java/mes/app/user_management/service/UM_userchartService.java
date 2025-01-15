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
public class UM_userchartService {

    @Autowired
    SqlRunner sqlRunner;

    public List<Map<String, Object>> getGridList(String startDate, String endDate, String searchUserNm) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
       SELECT
            USERSIDO AS region,
            USERGU AS district,
            SUM(CASE WHEN AGENUM <= 19 THEN 1 ELSE 0 END) AS age_10,
            SUM(CASE WHEN AGENUM BETWEEN 20 AND 29 THEN 1 ELSE 0 END) AS age_20,
            SUM(CASE WHEN AGENUM BETWEEN 30 AND 39 THEN 1 ELSE 0 END) AS age_30,
            SUM(CASE WHEN AGENUM BETWEEN 40 AND 49 THEN 1 ELSE 0 END) AS age_40,
            SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 THEN 1 ELSE 0 END) AS age_50,
            SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 THEN 1 ELSE 0 END) AS age_60,
            SUM(CASE WHEN AGENUM >= 70 THEN 1 ELSE 0 END) AS age_70,
            COUNT(*) AS total
        FROM TB_USERINFO
        WHERE 1=1
    """);

        // WHERE 절 동적 조건 추가
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND INDATEM >= :startDate ");
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND INDATEM <= :endDate ");
            params.addValue("endDate", endDate);
        }
        if (searchUserNm != null && !searchUserNm.isEmpty()) {
            sql.append(" AND (USERGU LIKE :searchUserNm OR USERSIDO LIKE :searchUserNm) ");
            params.addValue("searchUserNm", "%" + searchUserNm + "%");
        }

        // GROUP BY와 ORDER BY 추가
        sql.append("""
     GROUP BY
            USERSIDO,
            USERGU
        ORDER BY region, district
    """);

        // 로그로 SQL과 매개변수 확인
        log.info("Generated SQL: {}", sql.toString());
        log.info("SQL Parameters: {}", params.getValues());

        // 쿼리 실행 및 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }

}
