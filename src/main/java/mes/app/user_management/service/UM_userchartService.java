package mes.app.user_management.service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UM_userchartService {

    @Autowired
    SqlRunner sqlRunner;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getDynamicData(
        String startDate, String endDate, String inDatem, String sexYn, String district) {

        List<String> regions = Arrays.asList(
            "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시",
            "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원도",
            "충청북도", "충청남도", "전라북도", "전라남도", "경상북도", "경상남도", "제주특별자치도"
        );

        List<String> constructions = Arrays.asList("아파트", "빌라", "단독", "오피스", "기타");

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        List<String> selectColumns = new ArrayList<>();
        List<String> groupByColumns = new ArrayList<>();

        String inDatemColumn = "sub.inDatem";
        if ("Year".equals(inDatem)) {
            inDatemColumn = "LEFT(sub.inDatem, 4)";
        } else if ("Month".equals(inDatem)) {
            inDatemColumn = "LEFT(sub.inDatem, 7)";
        } else if ("Day".equals(inDatem)) {
            inDatemColumn = "LEFT(sub.inDatem, 10)";
        }

        if (inDatem != null) {
            selectColumns.add(inDatemColumn + " AS inDatem");
            groupByColumns.add(inDatemColumn);
        }

        if (district != null) {
            selectColumns.add("sub.district");
            groupByColumns.add("sub.district");
        }
        if (sexYn != null) {
            selectColumns.add("sub.sexYn");
            groupByColumns.add("sub.sexYn");
        }

        sql.append(String.join(", ", selectColumns));
        sql.append(",\n");

        List<String> countColumns = new ArrayList<>();
        for (String region : regions) {
            for (String construction : constructions) {
                String columnAlias = region.replaceAll("[^가-힣a-zA-Z]", "") + "_" + construction;
                String countQuery = String.format(
                    "COUNT(CASE WHEN sub.region LIKE '%s%%' AND sub.REALGUBUN = '%s' THEN 1 END) AS %s",
                    region, construction, columnAlias
                );
                sql.append(countQuery).append(",\n");
                countColumns.add(columnAlias);
            }
        }

        // ✅ total 컬럼을 동적으로 생성하는 방식 수정
        sql.append("(\n    ");
        for (String construction : constructions) {
            sql.append(String.format(
                "COUNT(CASE WHEN sub.REALGUBUN = '%s' THEN 1 END) +\n    ",
                construction
            ));
        }

// 🔹 마지막 `+` 제거
        int lastPlusIndex = sql.lastIndexOf("+");
        if (lastPlusIndex != -1) {
            sql.deleteCharAt(lastPlusIndex);
        }

        sql.append("\n) AS total ");

        sql.append("\nFROM (\n");
        sql.append("    SELECT \n");

        List<String> subQueryColumns = new ArrayList<>();
        if (inDatem != null) {
            subQueryColumns.add("CONVERT(DATE, RI.RELASTDATE) AS inDatem");
        }
        if (district != null) {
            subQueryColumns.add("RI.REGUGUN AS district");
        }
        if (sexYn != null) {
            subQueryColumns.add("TU.SEXYN AS sexYn");
        }
        subQueryColumns.add("COALESCE(TRIM(RI.RESIDO), '') AS region");
        subQueryColumns.add("COALESCE(TRIM(RI.REALGUBUN), '기타') AS REALGUBUN");

        sql.append(String.join(", ", subQueryColumns));
        sql.append("\n    FROM TB_REALINFO RI\n");
        sql.append("    JOIN TB_USERINFO TU ON TU.USERID = RI.USERID ");
        sql.append("    WHERE RI.RELASTDATE >= :startDate AND RI.RELASTDATE <= :endDate \n");

        if (inDatem != null) {
            sql.append("       AND RI.RELASTDATE <= :inDatem \n");
        }
        if (sexYn != null) {
            sql.append("      AND TU.SEXYN LIKE :sexYn \n");
        }
        if (district != null) {
            sql.append("       AND RI.REGUGUN LIKE :district\n");
        }

        sql.append(") AS sub\n");

        if (!groupByColumns.isEmpty()) {
            sql.append(" GROUP BY ").append(String.join(", ", groupByColumns));
        }

        return jdbcTemplate.queryForList(sql.toString(), new MapSqlParameterSource()
            .addValue("startDate", startDate)
            .addValue("endDate", endDate)
            .addValue("inDatem", inDatem)
            .addValue("sexYn", sexYn)
            .addValue("district", district)
        );
    }


    public List<Map<String, Object>> getGridListDynamic(
        String startDate, String endDate, String sexYn, String inDatem, String district, String region) {

        List<String> ageGroups = Arrays.asList("20대 이하", "20대", "30대", "40대", "50대", "60대", "70대 이상");
        List<String> constructions = Arrays.asList("아파트", "빌라", "단독", "오피스", "기타");

        // SQL 생성
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        // 동적으로 선택된 컬럼 추가
        List<String> selectColumns = new ArrayList<>();
        List<String> groupByColumns = new ArrayList<>();

        String inDatemColumn = "sub.inDatem";
        if ("Year".equals(inDatem)) {
            inDatemColumn = "LEFT(sub.inDatem, 4)";
        } else if ("Month".equals(inDatem)) {
            inDatemColumn = "LEFT(sub.inDatem, 7)";
        } else if ("Day".equals(inDatem)) {
            inDatemColumn = "LEFT(sub.inDatem, 10)";
        }

        if (inDatem != null) {
            selectColumns.add(inDatemColumn + " AS inDatem");
            groupByColumns.add(inDatemColumn);
        }

        if (region != null) {
            selectColumns.add("sub.region");
            groupByColumns.add("sub.region");
        }
        if (district != null) {
            selectColumns.add("sub.district");
            groupByColumns.add("sub.district");
        }
        if (sexYn != null) {
            selectColumns.add("sub.sexYn");
            groupByColumns.add("sub.sexYn");
        }

        sql.append(String.join(", ", selectColumns));
        sql.append(", ");

        for (String age : ageGroups) {
            String ageKey = age.replaceAll("[^0-9]", "");
            for (String construction : constructions) {
                String columnAlias = "age_" + ageKey + "_" + construction;
                sql.append(String.format(
                    "SUM(CASE WHEN sub.AGE_GROUP = '%s' AND sub.REALGUBUN = '%s' THEN 1 ELSE 0 END) AS %s, ",
                    age, construction, columnAlias
                ));
            }
        }

        // 전체 합계 컬럼 추가
        sql.append("SUM(");

        for (String age : ageGroups) {
            String ageKey = age.replaceAll("[^0-9]", "");
            for (String construction : constructions) {
                sql.append(String.format(
                    "(CASE WHEN sub.AGE_GROUP = '%s' AND sub.REALGUBUN = '%s' THEN 1 ELSE 0 END) + ",
                    age, construction
                ));
            }
        }

        // 🔹 마지막 `+` 제거 (마지막 값 뒤에 불필요한 연산자가 있으면 오류 발생)
        int lastPlusIndex = sql.lastIndexOf("+");
        if (lastPlusIndex != -1) {
            sql.deleteCharAt(lastPlusIndex);
        }

        sql.append(") AS total ");


        // 서브쿼리 추가
        sql.append("\nFROM (\n");
        sql.append("    SELECT \n");

        List<String> subQueryColumns = new ArrayList<>();
        if (inDatem != null) {
            subQueryColumns.add("CONVERT(DATE, RI.RELASTDATE) AS inDatem");
        }
        if (region != null) {
            subQueryColumns.add("RI.RESIDO AS region");
        }
        if (district != null) {
            subQueryColumns.add("RI.REGUGUN AS district");
        }
        if (sexYn != null) {
            subQueryColumns.add("TU.SEXYN AS sexYn");
        }

        subQueryColumns.add("RI.REALGUBUN");
        subQueryColumns.add("TU.AGENUM");
        subQueryColumns.add("""
        CASE
            WHEN TU.AGENUM < 20 THEN '20대 이하'
            WHEN TU.AGENUM BETWEEN 20 AND 29 THEN '20대'
            WHEN TU.AGENUM BETWEEN 30 AND 39 THEN '30대'
            WHEN TU.AGENUM BETWEEN 40 AND 49 THEN '40대'
            WHEN TU.AGENUM BETWEEN 50 AND 59 THEN '50대'
            WHEN TU.AGENUM BETWEEN 60 AND 69 THEN '60대'
            ELSE '70대 이상'
        END AS AGE_GROUP
    """);

        sql.append(String.join(", ", subQueryColumns));
        sql.append("\n    FROM TB_REALINFO RI\n");
        sql.append("join TB_USERINFO TU ON TU.USERID = RI.USERID ");
        sql.append("    WHERE RI.RELASTDATE >= :startDate AND RI.RELASTDATE <= :endDate \n");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);

        if (inDatem != null && !inDatem.isEmpty()) {
            sql.append("       AND RI.RELASTDATE <= :inDatem  \n");
            params.addValue("inDatem", inDatem);
        }
        if (sexYn != null) {
            params.addValue("sexYn", sexYn);
        }
        if(region != null){
            params.addValue("region", region );
        }
        if (district != null) {
            params.addValue("district", district);
        }

        sql.append(") AS sub\nGROUP BY ").append(String.join(", ", groupByColumns));

        //log.info("그리드 리스트 SQL: {}", sql.toString());
        //log.info("SQL 매개변수: {}", params.getValues());

        return sqlRunner.getRows(sql.toString(), params);
    }


    //엑셀 다운 정보
    public List<Map<String, Object>> getUserInfo(String startDate, String endDate, String region, String district, String ageGroup) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        // 지역명 변환 적용 (예: "서울특별시" → "서울")
        region = convertRegionName(region);

        //  나이 그룹 변환
        int minAge = 0, maxAge = 0;
        if (ageGroup != null) {
            switch (ageGroup) {
                case "10대 이하":
                    minAge = 0; maxAge = 19;
                    break;
                case "20대":
                    minAge = 20; maxAge = 29;
                    break;
                case "30대":
                    minAge = 30; maxAge = 39;
                    break;
                case "40대":
                    minAge = 40; maxAge = 49;
                    break;
                case "50대":
                    minAge = 50; maxAge = 59;
                    break;
                case "60대":
                    minAge = 60; maxAge = 69;
                    break;
                case "70대 이상":
                    minAge = 70; maxAge = 150;
                    break;
            }
        }

        StringBuilder sql = new StringBuilder("""
    SELECT
        USERNM AS 이름,
        AGENUM AS 연령,
        USERSIDO AS 지역,
        USERGU AS 구군,
        USERMAIL AS 이메일,
        CASE SEXYN
           WHEN 1 THEN '남자'
           WHEN 2 THEN '여자'
           ELSE '알 수 없음'
       END AS 성별
    FROM TB_USERINFO
    WHERE
        USERSIDO = :region  --  변환된 지역명 사용
        AND USERGU LIKE CONCAT('%', :district, '%')    
        AND AGENUM BETWEEN :minAge AND :maxAge 
        AND (:startDate IS NULL OR INDATEM >= :startDate)
        AND (:endDate IS NULL OR INDATEM <= :endDate)        
    ORDER BY INDATEM;
    """);

        // SQL 매개변수 설정
        params.addValue("region", region);
        params.addValue("district", "%" + district + "%");  // LIKE 검색
        params.addValue("minAge", minAge);
        params.addValue("maxAge", maxAge);
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);

        // 디버깅 로그 추가
        log.info("엑셀 다운 정보SQL: {}", sql.toString());
        log.info("SQL 매개변수: {}", params.getValues());

        return sqlRunner.getRows(sql.toString(), params);
    }

    public String convertRegionName(String region) {
        if (region == null) return null;

        //지역명 변환 매핑
        Map<String, String> regionMap = Map.ofEntries(
            Map.entry("서울특별시", "서울"),
            Map.entry("부산광역시", "부산"),
            Map.entry("인천광역시", "인천"),
            Map.entry("대구광역시", "대구"),
            Map.entry("광주광역시", "광주"),
            Map.entry("대전광역시", "대전"),
            Map.entry("울산광역시", "울산"),
            Map.entry("세종특별자치시", "세종"),
            Map.entry("경기도", "경기"),
            Map.entry("강원도", "강원"),
            Map.entry("충청북도", "충북"),
            Map.entry("충청남도", "충남"),
            Map.entry("전라북도", "전북"),
            Map.entry("전라남도", "전남"),
            Map.entry("경상북도", "경북"),
            Map.entry("경상남도", "경남"),
            Map.entry("제주특별자치도", "제주")
        );

        return regionMap.getOrDefault(region, region);  // 매핑된 값이 없으면 원래 값 유지
    }
}
