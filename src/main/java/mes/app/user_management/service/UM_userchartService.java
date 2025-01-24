package mes.app.user_management.service;


import lombok.extern.slf4j.Slf4j;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UM_userchartService {

    @Autowired
    SqlRunner sqlRunner;

    /*public List<Map<String, Object>> getGridList(String startDate, String endDate, List<String> columns) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("SELECT ");

        // 동적 SELECT 및 GROUP BY 컬럼을 저장할 리스트 초기화
        List<String> mappedColumns = new ArrayList<>();

        // 동적으로 SELECT 절 생성
        if (columns == null || columns.isEmpty()) {
            // 기본 컬럼 설정
            sql.append("""
            inDatem,
              region,
              district,
              AGE_GROUP,
              REALGUBUN,
              sexYn,
        """);
//            mappedColumns.addAll(List.of("INDATEM AS inDatem", "RESIDO AS region", "REGUGUN AS district", "AGE_GROUP", "REALGUBUN", "SEXYN AS sexYn"));
        } else {
            // 선택된 컬럼만 포함
            for (String column : columns) {
                switch (column) {
                    case "inDatem":
                        mappedColumns.add("INDATEM AS inDatem");
                        break;
                    case "region":
                        mappedColumns.add("RESIDO AS region");
                        break;
                    case "district":
                        mappedColumns.add("REGUGUN AS district");
                        break;
                    case "ageGroup":
                        mappedColumns.add("AGE_GROUP");
                        break;
                    case "realGubun":
                        mappedColumns.add("REALGUBUN");
                        break;
                    case "sexYn":
                        mappedColumns.add("SEXYN AS sexYn");
                        break;
                    default:
                        log.warn("Unknown column: {}", column); // 잘못된 컬럼 경고
                }
            }
            if (!mappedColumns.isEmpty()) {
                sql.append(String.join(", ", mappedColumns)).append(", ");
            }
        }

        // 고정 컬럼 (집계 컬럼 등)
        sql.append("""
        COUNT(*) AS total,
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
          -- 50대 구축물 유형
          SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_50_apartment,
          SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_50_villa,
          SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_50_house,
          SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_50_office,
          SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_50_other,
          -- 60대 구축물 유형
          SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_60_apartment,
          SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_60_villa,
          SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_60_house,
          SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_60_office,
          SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_60_other,
          -- 70대 이상 구축물 유형
          SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_70_apartment,
          SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_70_villa,
          SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_70_house,
          SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_70_office,
          SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_70_other
    FROM (
        SELECT
            tu.INDATEM AS inDatem,  -- 여기서 반환된 inDatem을 참조
            tr.RESIDO AS region,
            tr.REGUGUN AS district,
            tr.REALGUBUN,
            tu.AGENUM,
            tu.SEXYN AS sexYn,
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

        // 조건 추가
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND tu.INDATEM >= :startDate ");
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND tu.INDATEM <= :endDate ");
            params.addValue("endDate", endDate);
        }

        sql.append("""
        ) AS SubQuery
      GROUP BY inDatem, region, district, AGE_GROUP, REALGUBUN, sexYn
      ORDER BY region, district;
    """);

        // 로그로 SQL과 매개변수 확인
        log.info("그리드 리스트 SQL: {}", sql.toString());
        log.info("SQL 매개변수: {}", params.getValues());

        // 쿼리 실행 및 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }*/
    public List<Map<String, Object>> getGridList(String startDate, String endDate, List<String> columns) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("SELECT ");

        // 동적 SELECT 및 GROUP BY 컬럼을 저장할 리스트 초기화
        List<String> mappedColumns = new ArrayList<>();

        // 기본 컬럼 추가
        List<String> defaultColumns = List.of(
            "tu.INDATEM AS inDatem",
            "tr.RESIDO AS region",
            "tr.REGUGUN AS district",
            "AGE_GROUP",
            "REALGUBUN",
            "tu.SEXYN AS sexYn"
        );

        // SELECT 절 동적 생성
        if (columns == null || columns.isEmpty()) {
            sql.append(String.join(", ", defaultColumns));
        } else {
            List<String> selectedColumns = new ArrayList<>();
            for (String column : columns) {
                switch (column) {
                    case "inDatem": selectedColumns.add("tu.INDATEM AS inDatem"); break;
                    case "region": selectedColumns.add("tr.RESIDO AS region"); break;
                    case "district": selectedColumns.add("tr.REGUGUN AS district"); break;
                    case "ageGroup": selectedColumns.add("AGE_GROUP"); break;
                    case "realGubun": selectedColumns.add("REALGUBUN"); break;
                    case "sexYn": selectedColumns.add("tu.SEXYN AS sexYn"); break;
                    default: log.warn("Unknown column: {}", column);
                }
            }
            sql.append(String.join(", ", selectedColumns));
        }

        // 고정 컬럼 (집계 컬럼 등)
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
        -- 50대 구축물 유형
        SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_50_apartment,
        SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_50_villa,
        SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_50_house,
        SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_50_office,
        SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_50_other,
        -- 60대 구축물 유형
        SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_60_apartment,
        SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_60_villa,
        SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_60_house,
        SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_60_office,
        SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_60_other,
        -- 70대 이상 구축물 유형
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
            tu.SEXYN AS sexYn,
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

        // 조건 추가
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND tu.INDATEM >= :startDate ");
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND tu.INDATEM <= :endDate ");
            params.addValue("endDate", endDate);
        }

        sql.append("""
    ) AS SubQuery
    GROUP BY tu.INDATEM, tr.RESIDO, tr.REGUGUN, AGE_GROUP, REALGUBUN, tu.SEXYN
    ORDER BY tr.RESIDO, tr.REGUGUN;
    """);

        // 로그로 SQL과 매개변수 확인
        log.info("그리드 리스트 SQL: {}", sql.toString());
        log.info("SQL 매개변수: {}", params.getValues());

        // 쿼리 실행 및 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }

    //디비에서 작동되는 쿼리문
   /* SELECT
        inDatem,
        region,
        district,
        AGE_GROUP,
        REALGUBUN,
        sexYn,
    COUNT(*) AS total,
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
      -- 50대 구축물 유형
    SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_50_apartment,
    SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_50_villa,
    SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_50_house,
    SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_50_office,
    SUM(CASE WHEN AGENUM BETWEEN 50 AND 59 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_50_other,
      -- 60대 구축물 유형
    SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_60_apartment,
    SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_60_villa,
    SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_60_house,
    SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_60_office,
    SUM(CASE WHEN AGENUM BETWEEN 60 AND 69 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_60_other,
      -- 70대 이상 구축물 유형
    SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '아파트' THEN 1 ELSE 0 END) AS age_70_apartment,
    SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '빌라' THEN 1 ELSE 0 END) AS age_70_villa,
    SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '단독' THEN 1 ELSE 0 END) AS age_70_house,
    SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '오피스' THEN 1 ELSE 0 END) AS age_70_office,
    SUM(CASE WHEN AGENUM >= 70 AND REALGUBUN = '기타' THEN 1 ELSE 0 END) AS age_70_other
    FROM (
        SELECT
            tu.INDATEM AS inDatem,  -- 여기서 반환된 inDatem을 참조
            tr.RESIDO AS region,
        tr.REGUGUN AS district,
        tr.REALGUBUN,
        tu.AGENUM,
        tu.SEXYN AS sexYn,
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
            AND tu.INDATEM >= '2025-01-01'
            AND tu.INDATEM <= '2025-01-31'
    ) AS SubQuery
    GROUP BY inDatem, region, district, AGE_GROUP, REALGUBUN, sexYn
    ORDER BY region, district;*/

    public List<Map<String, Object>> getUserInfo(String startDate, String endDate, String region, String district, String ageGroup) {
        MapSqlParameterSource params = new MapSqlParameterSource();
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
            USERSIDO = :region 
            AND USERGU = :district                 
            AND AGENUM BETWEEN :minAge AND :maxAge 
            AND (:startDate IS NULL OR INDATEM >= :startDate) -
            AND (:endDate IS NULL OR INDATEM <= :endDate)     -- 날짜 조건 
        ORDER BY INDATEM;
    """);

        // 나이 그룹 매핑
        int minAge = 0, maxAge = 0;
        if (ageGroup != null) {
            switch (ageGroup) {
                case "10대 이하":
                    minAge = 0;
                    maxAge = 19;
                    break;
                case "20대":
                    minAge = 20;
                    maxAge = 29;
                    break;
                case "30대":
                    minAge = 30;
                    maxAge = 39;
                    break;
                case "40대":
                    minAge = 40;
                    maxAge = 49;
                    break;
                case "50대":
                    minAge = 50;
                    maxAge = 59;
                    break;
                case "60대":
                    minAge = 60;
                    maxAge = 69;
                    break;
                case "70대 이상":
                    minAge = 70;
                    maxAge = 150; // 상한 값 설정
                    break;
            }
        }

        // 매개변수 설정
        params.addValue("region", region);
        params.addValue("district", district);
        params.addValue("minAge", minAge);
        params.addValue("maxAge", maxAge);
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);

        // SQL 실행
        return sqlRunner.getRows(sql.toString(), params);
    }

}
