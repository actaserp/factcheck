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
       WITH RankedData AS (
                   SELECT\s
                       tr.RESIDO,
                       tr.REGUGUN,
                       tr.REALADD AS address,
                       tr2.PinNo,
                       tr.REALID,
                       tr.realscore,
                       COUNT(*) OVER (PARTITION BY tr2.PinNo) AS pinno_count,  -- PinNo별 개수 계산
                       ROW_NUMBER() OVER (PARTITION BY tr2.PinNo ORDER BY tr.REALID DESC) AS rn
                   FROM TB_REALINFO tr
                   LEFT JOIN TB_REALINFOXML tr2\s
                       ON tr2.REALID = tr.REALID
                   WHERE REPLACE(RESIDO, ' ', '') LIKE CONCAT(REPLACE(:region, ' ', ''), '%')
                     AND (:gugun IS NULL OR REPLACE(REGUGUN, ' ', '') LIKE CONCAT(REPLACE(:gugun, ' ', ''), '%'))
               )
               SELECT\s
               	REALID,
                   RESIDO,
                   REGUGUN,
                   address,
                   PinNo,
                   pinno_count AS count,  -- PinNo 기준으로 개수 계산된 값 사용
                   AVG(realscore) AS avg_score
               FROM RankedData
               WHERE rn = 1
                 AND PinNo IS NOT NULL  -- NULL 값 제거
               GROUP BY REALID,RESIDO, REGUGUN, address, PinNo, pinno_count;
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
                WITH FilteredRealInfo AS (
                    SELECT
                        RI.REALID, RI.RESIDO, RI.REGUGUN, RI.REALADD, RI.REALSCORE, RI.REQDATE
                    FROM TB_REALINFO RI
                    WHERE RI.RESIDO LIKE :resido
                    AND (COALESCE(:regugun, '') = '' OR RI.REGUGUN = :regugun)
                ),
                RankedRealInfo AS (
                    SELECT
                        RIX.REALID,
                        RIX.PinNo,
                        RIX.WksbiAddress,
                        RIX.Address,
                        MAX(RIX.REALID) OVER (PARTITION BY RIX.PinNo) AS MaxRealID
                    FROM TB_REALINFOXML RIX
                    JOIN FilteredRealInfo RI ON RI.REALID = RIX.REALID
                )
                SELECT
                	RI.REALID,
                    RI.RESIDO,
                    RI.REGUGUN,
                    RI.REALADD,
                    RI.REALSCORE,
                    RI.REQDATE,
                    RIX.PinNo,
                    RIX.WksbiAddress,
                    RIX.Address,
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
                FROM RankedRealInfo RIX
                JOIN TB_REALINFO RI ON RI.REALID = RIX.MaxRealID
                WHERE RIX.REALID = RIX.MaxRealID
                ORDER BY RI.REALSCORE DESC;
    """;

        return sqlRunner.getRows(sql, params);
    }

    public List<Map<String, Object>> getAOwn(int realid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("realid", realid);

        String sql = """
            SELECT * FROM TB_REALAOWN
            WHERE REALID = :realid
            ORDER BY
                TRY_CAST(REPLACE(RankNo, '-', '') +
                                         CASE WHEN RankNo LIKE '%-%' THEN '' ELSE '0' END AS INT) ASC,
                                CASE WHEN ISNUMERIC(RankNo) = 1 THEN 0 ELSE 1 END,
                                RankNo ASC;
    """;

        return sqlRunner.getRows(sql, params);
    }

    public List<Map<String, Object>> getBOwn(int realid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("realid", realid);

        String sql = """
            SELECT * FROM TB_REALBOWN
            WHERE REALID = :realid
            ORDER BY
                TRY_CAST(REPLACE(RankNo, '-', '') +
                                         CASE WHEN RankNo LIKE '%-%' THEN '' ELSE '0' END AS INT) ASC,
                                CASE WHEN ISNUMERIC(RankNo) = 1 THEN 0 ELSE 1 END,
                                RankNo ASC;
    """;

        return sqlRunner.getRows(sql, params);
    }

    public List<Map<String, Object>> getRegionList(String region, String gugun) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        // 현재 날짜 가져오기
        LocalDate today = LocalDate.now();

        // SQL 쿼리 빌드
        String sql = """
         WITH RankedData AS (
             SELECT
                 tr.REALADD,
                 tr.INDATEM,
                 tr.REALID,
                 tr2.PinNo,
                 ROW_NUMBER() OVER (PARTITION BY tr2.PinNo ORDER BY tr.REALID DESC) AS rn
             FROM TB_REALINFO tr
             LEFT JOIN TB_REALINFOXML tr2
                 ON tr2.REALID = tr.REALID
             WHERE REPLACE(RESIDO, ' ', '') LIKE '%' + REPLACE(:region, ' ', '') + '%'
               AND REPLACE(REGUGUN, ' ', '') LIKE '%' + REPLACE(:gugun, ' ', '') + '%'
               AND INDATEM <= :endDate
         )
         SELECT REALADD, INDATEM, REALID, PinNo
         FROM RankedData
         WHERE rn = 1
           AND PinNo IS NOT NULL 
         ORDER BY INDATEM DESC;
    """;

        // 파라미터 설정
        params.addValue("region", region);
        params.addValue("gugun", gugun);  // 구군 필터 추가
        params.addValue("endDate", today);  // 현재 날짜 설정

        // 로그 출력
//        log.info("검색지역 리스트 SQL: {}", sql);
//        log.info("SQL Parameters: {}", params.getValues());

        // 결과 반환
        return sqlRunner.getRows(sql, params);
    }

}
