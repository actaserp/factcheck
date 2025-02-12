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

    List<String> constructions = Arrays.asList("아파트", "다세대주택", "단독주택", "오피스텔");

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
      selectColumns.add("CASE WHEN sub.sexYn = '1' THEN '남자' WHEN sub.sexYn = '2' THEN '여자' ELSE '알 수 없음' END AS sexYn");
      groupByColumns.add("sub.sexYn");
    }

    sql.append(String.join(", ", selectColumns));
    sql.append(",\n");

    List<String> countColumns = new ArrayList<>();
    for (String region : regions) {
      for (String construction : constructions) {
        String columnAlias = region.replaceAll("[^가-힣a-zA-Z]", "") + "_" + construction;

        // countQuery 변수를 먼저 선언
        String countQuery = String.format(
            "COUNT(CASE WHEN REPLACE(sub.region, ' ', '') LIKE REPLACE('%s%%', ' ', '') AND sub.REALGUBUN = '%s' THEN 1 END) AS %s",
            region, construction, columnAlias
        );

        sql.append(countQuery).append(",\n"); // ✅ 올바른 순서
        countColumns.add(columnAlias);
      }

      // "기타" 항목을 한 번만 추가
      String columnAlias = region.replaceAll("[^가-힣a-zA-Z]", "") + "_기타";
      String countQuery = String.format(
          "COUNT(CASE WHEN REPLACE(sub.region, ' ', '') LIKE '%%%s%%' " +
              "AND (COALESCE(TRIM(sub.REALGUBUN), '기타') NOT IN ('아파트', '다세대주택', '단독주택', '오피스텔') " +
              "OR COALESCE(TRIM(sub.REALGUBUN), '기타') = '분류되지 않음') " +
              "THEN 1 END) AS %s",
          region.replaceAll("\\s+", ""), columnAlias
      );

      sql.append(countQuery).append(",\n"); // ✅ 올바른 순서
      countColumns.add(columnAlias);
    }

    sql.append("COUNT(*) AS total \n");

    sql.append("FROM (\n");
    sql.append("    SELECT \n");

    List<String> subQueryColumns = new ArrayList<>();
    if (inDatem != null) {
      subQueryColumns.add("CONVERT(DATE, RI.RELASTDATE) AS inDatem");
    }
    if (district != null) {
      subQueryColumns.add("RI.REGUGUN AS district");
    }
    if (sexYn != null) {
      subQueryColumns.add("COALESCE(TU.SEXYN, '') AS sexYn");
    }
    subQueryColumns.add("COALESCE(TRIM(RI.RESIDO), '') AS region");
    // "기타"를 변환하지 않고 원본 값을 유지
    subQueryColumns.add("TRIM(RI.REALGUBUN) AS REALGUBUN");

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

    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("startDate", startDate)
        .addValue("endDate", endDate)
        .addValue("inDatem", inDatem)
        .addValue("sexYn", sexYn)
        .addValue("district", district);

    log.info("그리드 리스트 SQL: {}", sql.toString());
    log.info("SQL 매개변수: {}", params.getValues());

    return jdbcTemplate.queryForList(sql.toString(), params);
  }

  public List<Map<String, Object>> getGridListDynamic(
      String startDate, String endDate, String sexYn, String inDatem, String district, String region) {

    List<String> ageGroups = Arrays.asList("19", "20", "30", "40", "50", "60", "70");
    List<String> constructions = Arrays.asList("아파트", "다세대주택", "단독주택", "오피스텔");

    StringBuilder sql = new StringBuilder();
    sql.append("SELECT ");

    List<String> selectColumns = new ArrayList<>();
    List<String> groupByColumns = new ArrayList<>();

    // 날짜 필드 설정
    String inDatemColumn = "LEFT(sub.inDatem, 10)";
    if ("Year".equals(inDatem)) {
      inDatemColumn = "LEFT(sub.inDatem, 4)";
    } else if ("Month".equals(inDatem)) {
      inDatemColumn = "LEFT(sub.inDatem, 7)";
    }
    selectColumns.add(inDatemColumn + " AS inDatem");
    groupByColumns.add(inDatemColumn);

    if (region != null) {
      selectColumns.add("COALESCE(sub.region, '') AS region");
      groupByColumns.add("COALESCE(sub.region, '')");
    }
    if (district != null) {
      selectColumns.add("COALESCE(sub.district, '') AS district");
      groupByColumns.add("COALESCE(sub.district, '')");
    }
    if (sexYn != null) {
      selectColumns.add("sub.sexYn");
      groupByColumns.add("sub.sexYn");
    }

    sql.append(String.join(", ", selectColumns));
    sql.append(",\n ");

    // 연령별 + 건축물별 + 기타 컬럼 생성
    for (String age : ageGroups) {
      for (String construction : constructions) {
        String columnAlias = "age_" + age + "_" + construction;
        sql.append(String.format(
            "COUNT(CASE WHEN sub.AGE_GROUP = '%s' AND sub.REALGUBUN = '%s' THEN 1 END) AS %s,\n ",
            age, construction, columnAlias
        ));
      }
      // 기타 항목 추가
      String ageEtcAlias = "age_" + age + "_기타";
      sql.append(String.format(
          "COUNT(CASE WHEN sub.AGE_GROUP = '%s' AND sub.REALGUBUN = '기타' THEN 1 END) AS %s, \n",
          age, ageEtcAlias
      ));
    }

    // 전체 합계 추가
    sql.append(" COUNT(*) AS total ");

    // 서브쿼리 시작
    sql.append("\nFROM (\n");
    sql.append("    SELECT \n");

    List<String> subQueryColumns = new ArrayList<>();
    subQueryColumns.add("CONVERT(DATE, RI.RELASTDATE) AS inDatem");
    subQueryColumns.add("RI.RESIDO AS region");
    subQueryColumns.add("RI.REGUGUN AS district");
    subQueryColumns.add("TU.SEXYN AS sexYn");

    // REALGUBUN을 "기타"로 변환
    subQueryColumns.add(
        "CASE " +
            "WHEN RI.REALGUBUN IS NULL OR LTRIM(RTRIM(RI.REALGUBUN)) = '' OR RI.REALGUBUN = '분류되지 않음' " +
            "OR RI.REALGUBUN NOT IN ('아파트', '다세대주택', '단독주택', '오피스텔') THEN '기타' " +
            "ELSE RI.REALGUBUN END AS REALGUBUN"
    );

    // ✅ AGENUM 변환 로직 추가
    subQueryColumns.add("""
    CASE 
        WHEN COALESCE(TU.AGENUM, 0) = 0 THEN 1 
        ELSE TU.AGENUM 
    END AS AGENUM
""");

// ✅ 연령대 변환 로직
    subQueryColumns.add("""
    CASE
        WHEN COALESCE(TU.AGENUM, 0) = 0 THEN '19'
        WHEN TU.AGENUM BETWEEN 1 AND 19 THEN '19'
        WHEN TU.AGENUM BETWEEN 20 AND 29 THEN '20'
        WHEN TU.AGENUM BETWEEN 30 AND 39 THEN '30'
        WHEN TU.AGENUM BETWEEN 40 AND 49 THEN '40'
        WHEN TU.AGENUM BETWEEN 50 AND 59 THEN '50'
        WHEN TU.AGENUM BETWEEN 60 AND 69 THEN '60'
        ELSE '70'
    END AS AGE_GROUP
""");

    sql.append(String.join(", ", subQueryColumns));
    sql.append("\n    FROM TB_REALINFO RI\n");
    sql.append("    JOIN TB_USERINFO TU ON TU.USERID = RI.USERID ");
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
    if (region != null) {
      params.addValue("region", region);
    }
    if (district != null) {
      params.addValue("district", district);
    }

    sql.append(") AS sub\nGROUP BY ").append(String.join(", ", groupByColumns));

//    log.info("그리드 리스트 SQL: {}", sql.toString());
//    log.info("SQL 매개변수: {}", params.getValues());

    return sqlRunner.getRows(sql.toString(), params);
  }

  //엑셀 다운정보
  public List<Map<String, Object>> getUserInfo(String yearMonth, String dateType, String region, String district, Integer sexYn, String selectedColumn) {
    MapSqlParameterSource params = new MapSqlParameterSource();

    // SQL 기본 구조
    StringBuilder sql = new StringBuilder("""
        SELECT 
            RI.userid AS 아이디,
            UI.usernm AS 이름,
            UI.USERHP AS 핸드폰_번호,
            UI.AGENUM AS 나이,
            CASE
                WHEN UI.sexyn = 1 THEN '남자'
                WHEN UI.sexyn = 2 THEN '여자'
                ELSE '알 수 없음'
            END AS 성별
        FROM TB_REALINFO RI
        JOIN TB_USERINFO UI ON RI.userid = UI.userid
        WHERE 1=1
    """);

    // 동적으로 `WHERE` 조건 추가
    if (region != null && !region.isEmpty()) {
      sql.append(" AND RI.RESIDO LIKE CONCAT('%', :region, '%')");
      params.addValue("region", region);
    }

    if (district != null && !district.isEmpty()) {
      sql.append(" AND RI.REGUGUN LIKE CONCAT('%', :district, '%')");
      params.addValue("district", district);
    }

    // `selectedColumn`에 따른 분기 처리
    if (selectedColumn != null && !selectedColumn.isEmpty()) {
      if ("기타".equals(selectedColumn)) {
        sql.append(" AND (RI.REALGUBUN NOT IN ('아파트', '다세대주택', '단독주택', '오피스텔') OR RI.REALGUBUN = '기타')");
      } else {
        sql.append(" AND RI.REALGUBUN = :selectedColumn");
        params.addValue("selectedColumn", selectedColumn);
      }
    }

    // 날짜 검색 방식 수정
    if (yearMonth != null && !yearMonth.isEmpty()) {
      if (yearMonth.length() == 4) {  // 연도만 입력된 경우 (예: 2025)
        sql.append(" AND LEFT(RI.RELASTDATE, 4) = :yearMonth");
      } else if (yearMonth.length() == 6) {  // 연도-월 입력된 경우 (예: 202501)
        sql.append(" AND LEFT(RI.RELASTDATE, 6) = :yearMonth");
      } else if (yearMonth.length() == 8) {  // 연도-월-일 입력된 경우 (예: 20250124)
        sql.append(" AND RI.RELASTDATE = :yearMonth");
      }
      params.addValue("yearMonth", yearMonth);
    }

    // dateType 조건 추가
    if (dateType != null && !dateType.isEmpty()) {
      sql.append(" AND RI.RELASTDATE = :dateType");
      params.addValue("dateType", dateType);
    }

    // 성별 필터 추가
    if (sexYn != null) {
      sql.append(" AND UI.SEXYN = :sexYn");
      params.addValue("sexYn", sexYn);
    }

    // 디버깅 로그 추가
    log.info("실행할 SQL: {}", sql.toString());
    log.info("SQL 매개변수: {}", params.getValues());

    return sqlRunner.getRows(sql.toString(), params);
  }
}
