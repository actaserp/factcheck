package mes.app.Map.service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MapService {
    @Autowired
    SqlRunner sqlRunner;

    public List<Map<String, Object>> getMarkersForRegion(String region, String gugun) {
        //MapSqlParameterSource 사용하여 SQL 파라미터 설정
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("region", region);
        params.addValue("gugun", gugun);

        return sqlRunner.getRows("""
      SELECT\s
             RESIDO,
             REGUGUN,
             REALADD as address,
             COUNT(*) as count,
             AVG(realscore) AS avg_score
         FROM TB_REALINFO
         WHERE RESIDO LIKE CONCAT(:region, '%')
         AND (:gugun IS NULL OR REGUGUN LIKE CONCAT(:gugun, '%'))
         GROUP BY RESIDO, REGUGUN, REALADD
    """, params);
    }


    public List<Map<String, Object>> getList(String name) {
        //MapSqlParameterSource 사용하여 SQL 파라미터 설정
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", name + "%");
        params.addValue("name_prefix", name);

        return sqlRunner.getRows("""
                  SELECT 
                    RESIDO, REGUGUN, REALSCORE, REALADD,
                    STUFF(REALADD, 1, LEN(:name_prefix), '') AS SHORT_ADDR
                  FROM TB_REALINFO
                  WHERE REALADD like :name;
                """, params);
    }

    public List<Map<String, Object>> getRegionList(String region, String gugun) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        // 현재 날짜 가져오기
        LocalDate today = LocalDate.now();

        // MSSQL용 SQL 작성 (문자열 연결은 `+` 사용)
        StringBuilder sql = new StringBuilder("""
          SELECT REALADD, RELASTDATE
          FROM TB_REALINFO
          WHERE RESIDO LIKE :region + '%'
            AND REGUGUN LIKE :gugun + '%'
            AND RELASTDATE <= :endDate
          ORDER BY RELASTDATE DESC;
      """);

        // 파라미터 설정
        params.addValue("region", region);
        params.addValue("gugun", gugun);
        params.addValue("endDate", today);  // 현재 날짜 설정

        //log.info("검색지역 리스트 SQL: {}", sql);
        //log.info("SQL Parameters: {}", params.getValues());

        // 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }

}
