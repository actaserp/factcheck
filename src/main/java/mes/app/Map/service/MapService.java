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


    public List<Map<String, Object>> getList(String resido, String regugun) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        // 검색할 resido 추출
        String searchResido;
        String[] residoParts = resido.split(" "); // 공백 기준으로 분리

        if (resido.endsWith("시") && residoParts.length > 1) {
            // "충청북도 청주시" → "청주"
            searchResido = residoParts[residoParts.length - 1].substring(0, 2);
        } else {
            // "서울특별시" → "서울", "경기도" → "경기", "충청북도" → "충청"
            searchResido = resido.substring(0, 2);
        }

        if (regugun != null && !regugun.isEmpty()) {
            String[] parts = regugun.split(" ");
            String lastPart = parts[parts.length - 1];
            if (lastPart.endsWith("구") || lastPart.endsWith("군")) {
                regugun = lastPart;
            } else {
                regugun = null;
            }
        }

        params.addValue("resido", "%" + searchResido + "%"); // LIKE 검색 적용
        params.addValue("regugun", regugun);

        System.out.println("searchResido: " + searchResido);
        System.out.println("regugun: " + regugun);

        String sql = """
    SELECT
        RESIDO, REGUGUN, REALSCORE, REALADD, REALID,
        COUNT(*) OVER() AS total_count,
        AVG(REALSCORE) OVER() AS avg_score,

        CASE
            WHEN CHARINDEX(' ', REALADD) > 0
            THEN STUFF(REALADD, 1, CHARINDEX(' ', REALADD), '')
            ELSE REALADD
        END AS SHORT_ADDR_1,

        CASE
            WHEN :regugun IS NOT NULL
                AND CHARINDEX(' ',
                    STUFF(REALADD, 1, CHARINDEX(' ', REALADD), '')) > 0
            THEN STUFF(
                STUFF(REALADD, 1, CHARINDEX(' ', REALADD), ''),
                1, CHARINDEX(' ', STUFF(REALADD, 1, CHARINDEX(' ', REALADD), '')), ''
            )
            ELSE STUFF(REALADD, 1, CHARINDEX(' ', REALADD), '')
        END AS SHORT_ADDR

    FROM TB_REALINFO
    WHERE RESIDO LIKE :resido
    AND (COALESCE(:regugun, '') = '' OR REGUGUN = :regugun)
    ORDER BY REALSCORE DESC;
    """;

        return sqlRunner.getRows(sql, params);
    }


    public List<Map<String, Object>> getOwn(int realid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("realid", realid);

        String sql = """
            SELECT * FROM TB_REALBOWN
            WHERE REALID = :realid
            ORDER BY
                TRY_CAST(REPLACE(RankNo, '-', '') +
                                         CASE WHEN RankNo LIKE '%-%' THEN '' ELSE '0' END AS INT) DESC,
                                CASE WHEN ISNUMERIC(RankNo) = 1 THEN 0 ELSE 1 END,
                                RankNo DESC;
    """;

        return sqlRunner.getRows(sql, params);
    }

    public List<Map<String, Object>> getRegionList(String region, String gugun) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        // 현재 날짜 가져오기
        LocalDate today = LocalDate.now();

        // MSSQL용 SQL 작성 (문자열 연결은 `+` 사용)
        StringBuilder sql = new StringBuilder("""
          SELECT REALADD, RELASTDATE, REALID
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
