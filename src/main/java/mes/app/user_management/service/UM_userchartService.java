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

        // total 컬럼을 동적으로 생성하는 방식 수정
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

   public List<Map<String, Object>> getUserInfo(String yearMonth, String dateType, String region, String district, Integer sexYn, String selectedColumn) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        // SQL 기본 구조
        StringBuilder sql = new StringBuilder("""
        SELECT userid AS 아이디,
        usernm as 이름 ,
        CASE\s
          WHEN sexyn = 1 THEN '남자'
          WHEN sexyn = 2 THEN '여자'
          ELSE '알 수 없음'
      END AS 성별
        FROM TB_USERINFO
        WHERE userid IN (
            SELECT userid 
            FROM TB_REALINFO 
            WHERE 1=1 
    """);

        // 동적으로 `WHERE` 조건 추가
        if (region != null && !region.isEmpty()) {
            sql.append(" AND RESIDO LIKE CONCAT('%', :region, '%')");
            params.addValue("region", region);
        }

        if (district != null && !district.isEmpty()) {
            sql.append(" AND REGUGUN LIKE CONCAT('%', :district, '%')");
            params.addValue("district", district);
        }

        if (selectedColumn != null && !selectedColumn.isEmpty()) {
            sql.append(" AND TB_REALINFO.REALGUBUN LIKE :selectedColumn");
            params.addValue("selectedColumn", selectedColumn);
        }

        // 날짜 검색 방식 수정
        if (yearMonth != null && !yearMonth.isEmpty()) {
            if (yearMonth.length() == 4) {  // 연도만 입력된 경우 (예: 2025)
                sql.append(" AND LEFT(RELASTDATE, 4) = :yearMonth");
            } else if (yearMonth.length() == 7) {  // 연도-월 입력된 경우 (예: 2025-01)
                sql.append(" AND LEFT(RELASTDATE, 6) = REPLACE(:yearMonth, '-', '')");
            } else if (yearMonth.length() == 10) {  // 연도-월-일 입력된 경우 (예: 2025-01-28)
                sql.append(" AND RELASTDATE = REPLACE(:yearMonth, '-', '')");
            }
            params.addValue("yearMonth", yearMonth);
        }

        if (dateType != null && !dateType.isEmpty()) {
            sql.append(" AND RELASTDATE = REPLACE(:dateType, '-', '')");
            params.addValue("dateType", dateType);
        }

        if (sexYn != null) {
            sql.append(" AND SEXYN = :sexYn");
            params.addValue("sexYn", sexYn);
        }

        // SQL 닫기
        sql.append(" )");

        // 디버깅 로그 추가
        //log.info("🔍 실행할 SQL: {}", sql.toString());
        //log.info("📌 SQL 매개변수: {}", params.getValues());

        return sqlRunner.getRows(sql.toString(), params);
    }

}
