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

    /*public List<Map<String, Object>> getGridList(String startDate, String endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
             SELECT
                USERSIDO AS region,
                USERGU AS district,
                INDATEM,
                SEXYN ,
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
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND tu.INDATEM >= :startDate ");
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND tu.INDATEM <= :endDate ");
            params.addValue("endDate", endDate);
        }

        sql.append("""
        GROUP BY
                USERSIDO,
                SEXYN ,
                USERGU,
                INDATEM
            ORDER BY region, district;
    """);

        // 로그로 SQL과 매개변수 확인
        log.info("그리드 리스트 SQL: {}", sql.toString());
        log.info("SQL 매개변수: {}", params.getValues());

        // 쿼리 실행 및 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }*/


    public List<Map<String, Object>> getGridList(String startDate, String endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
        SELECT
            INDATEM AS inDatem,
            RESIDO AS region,
            REGUGUN AS district,
            AGE_GROUP,
            REALGUBUN,
            SEXYN as sexYn,
            COUNT(*) AS total,
            -- 10대 이하 구축물 유형
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
                tr.USERID,
                tr.RESIDO,
                tr.REGUGUN,
                tr.REALGUBUN,
                tu.AGENUM,
                tu.INDATEM as inDatem,
                tu.SEXYN as sexYn,
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
        GROUP BY
            INDATEM,
            RESIDO,
            REGUGUN,
            AGE_GROUP,
            REALGUBUN,
            SEXYN
        ORDER BY
            RESIDO,
            REGUGUN;
    """);

        // 로그로 SQL과 매개변수 확인
//        log.info("그리드 리스트 SQL: {}", sql.toString());
//        log.info("SQL 매개변수: {}", params.getValues());

        // 쿼리 실행 및 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }

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
            AND AGENUM BETWEEN :minAge AND :maxAge -- 나이 그룹 필터링
            AND (:startDate IS NULL OR INDATEM >= :startDate) -- 날짜 조건 
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
