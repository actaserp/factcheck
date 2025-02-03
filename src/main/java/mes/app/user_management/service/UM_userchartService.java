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
import java.util.stream.Collectors;

@Slf4j
@Service
public class UM_userchartService {

    @Autowired
    SqlRunner sqlRunner;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
  @Autowired
  private mes.app.precedence.service.PartnerCheckService partnerCheckService;

    public List<Map<String, Object>> getGridList(String startDate, String endDate, List<String> columns, String sexYn) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("SELECT ");

        // 기본 컬럼 정의 (동적으로 선택 가능)
        List<String> defaultColumns = List.of(
            "sub.inDatem",
            "sub.region",
            "sub.district",
            "sub.AGE_GROUP",
            "sub.REALGUBUN"
        );

        // 동적으로 선택된 컬럼 적용
        if (columns == null || columns.isEmpty()) {
            sql.append(String.join(", ", defaultColumns));
        } else {
            List<String> selectedColumns = new ArrayList<>();
            for (String column : columns) {
                switch (column) {
                    case "inDatem": selectedColumns.add("sub.inDatem"); break;
                    case "region": selectedColumns.add("sub.region"); break;
                    case "district": selectedColumns.add("sub.district"); break;
                    case "ageGroup": selectedColumns.add("sub.AGE_GROUP"); break;
                    case "realGubun": selectedColumns.add("sub.REALGUBUN"); break;
                    case "sexYn": selectedColumns.add("sub.sexYn"); break;
                    default: log.warn("Unknown column: {}", column);
                }
            }
            sql.append(String.join(", ", selectedColumns));
        }

        // 고정된 집계 컬럼 추가
        sql.append("""
       , COUNT(*) AS total,
        SUM(CASE WHEN AGENUM <= 19 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_10_apartment,
        SUM(CASE WHEN AGENUM <= 19 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_10_villa,
        SUM(CASE WHEN AGENUM <= 19 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_10_house,
        SUM(CASE WHEN AGENUM <= 19 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_10_office,
        SUM(CASE WHEN AGENUM <= 19 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_10_other,
        -- 20대 구축물 유형
        SUM(CASE WHEN AGENUM BETWEEN 20 AND 29 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_20_apartment,
        SUM(CASE WHEN AGENUM BETWEEN 20 AND 29 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_20_villa,
        SUM(CASE WHEN AGENUM BETWEEN 20 AND 29 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_20_house,
        SUM(CASE WHEN AGENUM BETWEEN 20 AND 29 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_20_office,
        SUM(CASE WHEN AGENUM BETWEEN 20 AND 29 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_20_other,
        -- 30대 구축물 유형
        SUM(CASE WHEN AGENUM BETWEEN 30 AND 39 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_30_apartment,
        SUM(CASE WHEN AGENUM BETWEEN 30 AND 39 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_30_villa,
        SUM(CASE WHEN AGENUM BETWEEN 30 AND 39 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_30_house,
        SUM(CASE WHEN AGENUM BETWEEN 30 AND 39 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_30_office,
        SUM(CASE WHEN AGENUM BETWEEN 30 AND 39 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_30_other,
        -- 40대 구축물 유형
        SUM(CASE WHEN AGENUM BETWEEN 40 AND 49 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_40_apartment,
        SUM(CASE WHEN AGENUM BETWEEN 40 AND 49 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_40_villa,
        SUM(CASE WHEN AGENUM BETWEEN 40 AND 49 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_40_house,
        SUM(CASE WHEN AGENUM BETWEEN 40 AND 49 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_40_office,
        SUM(CASE WHEN AGENUM BETWEEN 40 AND 49 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_40_other,
        -- 50대 구축물
        SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_50_apartment,
        SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_50_villa,
        SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_50_house,
        SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_50_office,
        SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_50_other,
        -- 60대
        SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_60_apartment,
        SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_60_villa,
        SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_60_house,
        SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_60_office,
        SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_60_other,
        -- 70대 이상
        SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_70_apartment,
        SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_70_villa,
        SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_70_house,
        SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_70_office,
        SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_70_other
    FROM (
        SELECT
            tu.INDATEM AS inDatem,
            tr.RESIDO AS region,
            tr.REGUGUN AS district,
            tr.REALGUBUN,
            tu.AGENUM,
    """);
        // sexYn이 `columns`에 포함된 경우만 SELECT에 추가
        if (columns != null && columns.contains("sexYn")) {
            sql.append("tu.SEXYN AS sexYn, ");
        }

        sql.append("""
            CASE
                WHEN tu.AGENUM < 20 THEN '20대 이하'
                WHEN tu.AGENUM BETWEEN 20 AND 29 THEN '20대'
                WHEN tu.AGENUM BETWEEN 30 AND 39 THEN '30대'
                WHEN tu.AGENUM BETWEEN 40 AND 49 THEN '40대'
                WHEN tu.AGENUM BETWEEN 50 AND 59 THEN '50대'
                WHEN tu.AGENUM BETWEEN 60 AND 69 THEN '60대'
                ELSE '70대 이상'
            END AS AGE_GROUP
        FROM TB_REALINFO tr
        LEFT JOIN TB_USERINFO tu ON tr.USERID = tu.USERID
        LEFT JOIN auth_user au ON tr.USERID = au.username
        WHERE 1=1
    """);

        // WHERE 조건 추가
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND tu.INDATEM >= :startDate ");
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND tu.INDATEM <= :endDate ");
            params.addValue("endDate", endDate);
        }
        if (sexYn != null && !sexYn.isEmpty()) {
            sql.append(" AND tu.SEXYN = :sexYn ");
            params.addValue("sexYn", sexYn);
        }

        sql.append("""
    ) AS sub
    GROUP BY sub.inDatem, sub.region, sub.district, sub.AGE_GROUP, sub.REALGUBUN
    """);

        // `sexYn`이 `columns`에 포함된 경우만 `GROUP BY`에 추가
        if (columns != null && columns.contains("sexYn")) {
            sql.append(", sub.sexYn");
        }

        sql.append(" ORDER BY sub.region, sub.district;");

        // 로그 출력
        //log.info("그리드 리스트 SQL: {}", sql.toString());
        //log.info("SQL 매개변수: {}", params.getValues());

        // SQL 실행
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

    //지역(시)/구축물 List
    public List<Map<String, Object>> getDynamicData(
        String startDate, String endDate, String inDatem, String sexYn, String district) {

        // 지역 목록
        List<String> regions = Arrays.asList(
            "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시",
            "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원도",
            "충청북도", "충청남도", "전라북도", "전라남도", "경상북도", "경상남도", "제주특별자치도"
        );

        // 구축물 유형 목록
        List<String> constructions = Arrays.asList("아파트", "빌라", "단독", "오피스", "기타");

        // SQL 생성
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        // 동적으로 선택된 컬럼 추가
        List<String> selectColumns = new ArrayList<>();
        List<String> groupByColumns = new ArrayList<>();

        String inDatemColumn = "sub.inDatem"; // 기본값 설정 (그대로 사용)

        if ("Year".equals(inDatem)) {
            inDatemColumn = "LEFT(sub.inDatem, 4)";
        } else if ("Month".equals(inDatem)) {
            inDatemColumn = "LEFT(sub.inDatem, 7)";
        } else if ("Day".equals(inDatem)) {
            inDatemColumn = "LEFT(sub.inDatem, 10)";
        }

        // inDatem이 선택된 경우에만 컬럼 추가
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
        sql.append(", ");

        // 지역 및 구축물 유형별 COUNT 컬럼 동적 생성
        for (String region : regions) {
            for (String construction : constructions) {
                String columnAlias = region.replaceAll("[^가-힣a-zA-Z]", "") + "_" + construction;
                sql.append(String.format(
                    "COUNT(CASE WHEN sub.region LIKE '%s%%' AND sub.REALGUBUN = '%s' THEN 1 END) AS %s,\n",
                    region, construction, columnAlias
                ));
            }
        }

        // 마지막 쉼표 제거
        int lastCommaIndex = sql.lastIndexOf(",");
        if (lastCommaIndex != -1) {
            sql.deleteCharAt(lastCommaIndex);
        }

        // 서브쿼리 추가
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
        if (district != null || sexYn != null || inDatem != null) {
            subQueryColumns.add("COALESCE(TRIM(RI.RESIDO), '') AS region");
        }
        subQueryColumns.add("COALESCE(TRIM(RI.REALGUBUN), '기타') AS REALGUBUN");

        sql.append(String.join(", ", subQueryColumns));
        sql.append("\n    FROM MOB_FACTCHK.dbo.TB_REALINFO RI\n");
        sql.append("join TB_USERINFO TU ON TU.USERID = RI.USERID ");
        sql.append("    WHERE RI.RELASTDATE >= :startDate AND RI.RELASTDATE <= :endDate \n");

        // 추가 필터 조건
        if (inDatem != null) {
            sql.append("       AND RI.RELASTDATE <=:inDatem  \n");
        }
        if (sexYn != null) {
            sql.append("      AND TU.SEXYN LIKE :sexYn \n");
        }
        if (district != null) {
            sql.append("       AND RI.REGUGUN LIKE :district\n");
        }

        sql.append(") AS sub\n");

        // GROUP BY
        if (!groupByColumns.isEmpty()) {
            sql.append(" GROUP BY ");
            sql.append(String.join(", ", groupByColumns));
        }

        // SQL 실행 파라미터 설정
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);

        if (inDatem != null) {
            params.addValue("inDatem", inDatem);
        }
        if (sexYn != null) {
            params.addValue("sexYn", sexYn);
        }
        if (district != null) {
            params.addValue("district", district);
        }

//        log.info("Generated SQL:\n{}", sql.toString());
//        log.info("SQL 매개변수: {}", params.getValues());

        return jdbcTemplate.queryForList(sql.toString(), params);
    }


}
