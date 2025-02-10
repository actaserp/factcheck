package mes.app.production.service;

import com.google.gson.Gson;
import mes.domain.entity.factcheckEntity.TB_REALINFO;
import mes.domain.services.SqlRunner;
import org.apache.xmlbeans.impl.xb.xsdschema.impl.PublicImpl;
import org.docx4j.wml.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductionService {

    @Autowired
    SqlRunner sqlRunner;

    public List<Map<String, Object>> getlist1(String startDate, String endDate, String property) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);

        String sql = """
                 SELECT
                         subquery.REALID,
                         MAX(subquery.RELASTDATE) AS RLASTDATE,
                         MAX(subquery.USERID) AS USERID,
                         MAX(subquery.REQDATE) AS REQDATE,
                         MAX(subquery.REALADD) AS REALADD,
                         MAX(subquery.REGDATE) AS REGDATE,
                         MAX(subquery.RESIDO) AS RESIDO,
                         MAX(subquery.REGUGUN) AS REGUGUN,
                         MAX(subquery.REMOK) AS REMOK,
                         MAX(subquery.REWON) AS REWON,
                         MAX(subquery.REWONDATE) AS REWONDATE,
                         MAX(subquery.REJIMOK) AS REJIMOK,
                         MAX(subquery.REAREA) AS REAREA,
                         MAX(subquery.REAMOUNT) AS REAMOUNT,
                         MAX(subquery.RESEQ) AS RESEQ,
                         MAX(subquery.REMAXAMT) AS REMAXAMT,
                         MAX(subquery.INDATEM) AS INDATEM,
                         MAX(subquery.REALSCORE) AS REALSCORE,
                         MAX(subquery.REALPOINT) AS REALPOINT,
                         MAX(subquery.REALGUBUN) AS REALGUBUN,
                         MAX(subquery.S_REQDATE) AS S_REQDATE,
                         MAX(subquery.S_USERID) AS S_USERID,
                         MAX(subquery.S_REALID) AS S_REALID,
                         MAX(subquery.view_count) AS view_count,
                         MAX(subquery.UniqueNo) AS UniqueNo,
                         MAX(subquery.Gubun) AS Gubun,
                         MAX(subquery.Address) AS Address,
                         MAX(subquery.PrintDate) AS PrintDate,
                         MAX(subquery.DataA) AS DataA,
                         MAX(subquery.DataK) AS DataK,
                         MAX(subquery.DataE) AS DataE,
                         MAX(subquery.RankNo) AS RankNo_A,
                         MAX(subquery.RgsAimCont) AS RgsAimCont_A,
                         MAX(subquery.Receve) AS Receve_A,
                         MAX(subquery.RgsCaus) AS RgsCaus_A,
                         MAX(subquery.NomprsAndEtc) AS NomprsAndEtc_A,
                         MAX(subquery.RankNo) AS RankNo_B,
                         MAX(subquery.RgsAimCont) AS RgsAimCont_B,
                         MAX(subquery.Receve) AS Receve_B,
                         MAX(subquery.RgsCaus) AS RgsCaus_B,
                         MAX(subquery.NomprsAndEtc) AS NomprsAndEtc_B
                     FROM (
                         SELECT
                             R.REALID,
                             R.RELASTDATE,
                             R.USERID,
                             R.REQDATE,
                             R.REALADD,
                             R.REGDATE,
                             R.RESIDO,
                             R.REGUGUN,
                             R.REMOK,
                             R.REWON,
                             R.REWONDATE,
                             R.REJIMOK,
                             R.REAREA,
                             R.REAMOUNT,
                             R.RESEQ,
                             R.REMAXAMT,
                             R.INDATEM,
                             R.REALSCORE,
                             R.REALPOINT,
                             R.REALGUBUN,
                             S.REQDATE AS S_REQDATE,
                             S.USERID AS S_USERID,
                             S.REALID AS S_REALID,
                             COUNT(*) OVER (PARTITION BY R.REALID) AS view_count,
                             RS.UniqueNo,
                             RS.Gubun,
                             RS.Address,
                             RS.PrintDate,
                             RS.DataA,
                             RS.DataK,
                             RS.DataE,
                             AOWN.RankNo AS RankNo_A,
                             AOWN.RgsAimCont AS RgsAimCont_A,
                             AOWN.Receve AS Receve_A,
                             AOWN.RgsCaus AS RgsCaus_A,
                             AOWN.NomprsAndEtc AS NomprsAndEtc_A,
                             BOWN.RankNo AS RankNo_B,
                             BOWN.RgsAimCont AS RgsAimCont_B,
                             BOWN.Receve AS Receve_B,
                             BOWN.RgsCaus AS RgsCaus_B,
                             BOWN.NomprsAndEtc AS NomprsAndEtc_B
                         FROM
                             TB_REALINFO R
                         JOIN
                             TB_SEARCHINFO S
                         ON
                             R.USERID = S.USERID
                         LEFT JOIN
                             TB_REALSUMMARY RS
                         ON
                             R.REALID = RS.REALID
                         LEFT JOIN
                             TB_REALAOWN AOWN
                         ON
                             R.REALID = AOWN.REALID
                         LEFT JOIN
                             TB_REALBOWN BOWN
                         ON
                             R.REALID = BOWN.REALID
                        WHERE
                                               S.REQDATE BETWEEN :startDate AND :endDate
                                               AND (
                                                   (:property = 'my_property' AND S.USERID = R.USERID )
                                                   OR (:property = 'recent_property'  AND S.USERID = R.USERID)
                                                   OR (:property = 'most_viewed_property')
                                                   OR (:property = 'popular_property')
                                               )
                     ) AS subquery
                     GROUP BY subquery.REALID
                     ORDER BY
                         CASE
                             WHEN :property = 'my_property' THEN MAX(subquery.RELASTDATE)
                             WHEN :property = 'recent_property' THEN MAX(subquery.RELASTDATE)
                             WHEN :property IN ('most_viewed_property', 'popular_property') THEN MAX(subquery.view_count)
                             ELSE NULL
                         END DESC,
                         MAX(subquery.RELASTDATE) DESC
                """;

        /*// 'most_viewed_property'일 경우 LIMIT 100 추가
        if ("my_property".equals(property)) {
            sql += "S.USERID = R.USERID";
        }*/

        List<Map<String, Object>> items = this.sqlRunner.getRows(sql.toString(), dicParam);
        return items;
    }


    public Map<String, Object> getlist(String startDate, String endDate, String property, String username) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);
        dicParam.addValue("userid", username);
        Map<String, Object> result = new HashMap<>();

        result.put("my_property", searchDataList(startDate, endDate, "my_property", username));
        result.put("recent_property", searchDataList(startDate, endDate, "recent_property", username));
        result.put("most_viewed_property", searchDataList(startDate, endDate, "most_viewed_property", username));
        result.put("popular_property", searchDataList(startDate, endDate, "popular_property", username));

        return result;
    }

    private List<Map<String, Object>> searchDataList(String startDate, String endDate, String property, String userid) {
        // searchType에 따라 다른 쿼리 호출
        switch (property) {
            case "my_property":
                return getmyData(startDate, endDate, property, userid);
            case "recent_property":
                return getrecentData(startDate, endDate, property);
            case "most_viewed_property":
                return getmostData(startDate, endDate, property);
            case "popular_property":
                return getpopularData(startDate, endDate, property);
            default:
                return Collections.emptyList();
        }
    }


    public List<Map<String, Object>> getmyData(String startDate, String endDate, String property, String username) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();

        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);
        dicParam.addValue("userid", username);
        System.out.println(username+"쿼리 진입했을때의 username확인");

        String sql = """
                WITH CTE AS (
                    SELECT
                        R.REALID,
                        R.USERID,
                        R.REQDATE,
                        R.REALADD,
                        R.REGDATE,
                        R.RESIDO,
                        R.REGUGUN,
                        R.REMOK,
                        R.REWON,
                        R.REWONDATE,
                        R.REJIMOK,
                        R.REAREA,
                        R.REAMOUNT,
                        R.RESEQ,
                        R.REMAXAMT,
                        R.INDATEM,
                        R.REALSCORE,
                        R.REALPOINT,
                        R.RELASTDATE,
                        R.REALGUBUN,
                        R.PDFFILENAME,
                        S.USERID AS Suserid,
                        S.REQDATE AS Sreqdate,
                        S.REALID AS Srealid,
                        RS.UniqueNo,
                        RS.Gubun,
                        RS.Address,
                        RS.PrintDate,
                        -- RankNo_a 관련 데이터 (NULL 허용)
                        RA.RankNo AS RankNo_a,
                        RA.RgsAimCont AS RgsAimCont_a,
                        RA.Receve AS Receve_a,
                        RA.RgsCaus AS RgsCaus_a,
                        RA.NomprsAndEtc AS NomprsAndEtc_a,
                        RB.RankNo AS RankNo_b,
                        RB.RgsAimCont AS RgsAimCont_b,
                        RB.Receve AS Receve_b,
                        RB.RgsCaus AS RgsCaus_b,
                        RB.NomprsAndEtc AS NomprsAndEtc_b,
                        U.USERNM,
                        ROW_NUMBER() OVER (PARTITION BY R.REALID ORDER BY S.REQDATE) AS rn
                    FROM TB_REALINFO R
                    INNER JOIN TB_SEARCHINFO S
                        ON R.REALID = S.REALID
                    LEFT JOIN TB_USERINFO U
                        ON S.USERID = U.USERID
                    LEFT JOIN TB_REALSUMMARY RS
                        ON R.REALID = RS.REALID
                    LEFT JOIN TB_REALAOWN RA
                        ON R.REALID = RA.REALID
                    LEFT JOIN TB_REALBOWN RB
                        ON R.REALID = RB.REALID
                    WHERE
                        S.USERID = :userid
                        AND S.REALID = R.REALID
                        AND S.REQDATE BETWEEN
                            CAST(:startDate AS DATETIME)
                            AND DATEADD(MILLISECOND, -1, DATEADD(DAY, 1, CAST(:endDate AS DATETIME)))
                )
                SELECT
                    CTE.REALID,
                    CTE.USERID,
                    CTE.REQDATE,
                    CTE.REALADD,
                    CTE.REGDATE,
                    CTE.RESIDO,
                    CTE.REGUGUN,
                    CTE.REMOK,
                    CTE.REWON,
                    CTE.REWONDATE,
                    CTE.REJIMOK,
                    CTE.REAREA,
                    CTE.REAMOUNT,
                    CTE.RESEQ,
                    CTE.REMAXAMT,
                    CTE.INDATEM,
                    CTE.REALSCORE,
                    CTE.REALPOINT,
                    CTE.RELASTDATE,
                    CTE.REALGUBUN,
                    CTE.PDFFILENAME,
                    CTE.Suserid,
                    CTE.Sreqdate,
                    CTE.Srealid,
                    CTE.UniqueNo,
                    CTE.Gubun,
                    CTE.Address,
                    CTE.PrintDate,
                    CTE.USERNM,
                    -- RankNo_a 데이터 배열 반환 (TB_REALSUMMARY와 연결된 경우만)
                    CASE WHEN CTE.UniqueNo IS NOT NULL THEN COALESCE((
                        SELECT DISTINCT
                            RankNo_a, RgsAimCont_a, Receve_a, RgsCaus_a, NomprsAndEtc_a
                        FROM CTE
                        WHERE CTE.RankNo_a IS NOT NULL AND CTE.UniqueNo IS NOT NULL
                        FOR JSON PATH
                    ), '[]') ELSE '[]' END AS RankNo_a_data,
                    -- RankNo_b 데이터 배열 반환 (TB_REALSUMMARY와 연결된 경우만)
                    CASE WHEN CTE.UniqueNo IS NOT NULL THEN COALESCE((
                        SELECT DISTINCT
                            RankNo_b, RgsAimCont_b, Receve_b, RgsCaus_b, NomprsAndEtc_b
                        FROM CTE
                        WHERE CTE.RankNo_b IS NOT NULL AND CTE.UniqueNo IS NOT NULL
                        FOR JSON PATH
                    ), '[]') ELSE '[]' END AS RankNo_b_data,
                    CTE.rn
                FROM CTE
                WHERE CTE.rn = 1;
                """;

        List<Map<String, Object>> items = this.sqlRunner.getRows(sql.toString(), dicParam);

        // RankNo_a_data와 RankNo_b_data가 null인 경우 빈 배열 문자열 "[]"로 설정 (중요)
        for (Map<String, Object> item : items) {
            if (item.get("RankNo_a_data") == null) {
                item.put("RankNo_a_data", "[]"); // 빈 JSON 배열 문자열 할당
            }
            if (item.get("RankNo_b_data") == null) {
                item.put("RankNo_b_data", "[]"); // 빈 JSON 배열 문자열 할당
            }
        }

        return items;
    }

    public List<Map<String, Object>> getrecentData(String startDate, String endDate, String property) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);

        String sql = """
                WITH SEARCH_CTE AS (
                    SELECT
                        S.REALID,
                        S.USERID,
                        S.REQDATE,
                        ROW_NUMBER() OVER (PARTITION BY S.REALID ORDER BY S.REQDATE DESC) AS rn
                    FROM TB_SEARCHINFO S
                ),
                COUNT_CTE AS (
                    SELECT
                        REALID,
                        COUNT(*) AS record_count
                    FROM TB_SEARCHINFO
                    GROUP BY REALID
                ),
                CTE AS (
                    SELECT
                        R.REALID,
                        R.USERID,
                        R.REQDATE,
                        R.REALADD,
                        R.REGDATE,
                        R.RESIDO,
                        R.REGUGUN,
                        R.REMOK,
                        R.REWON,
                        R.REWONDATE,
                        R.REJIMOK,
                        R.REAREA,
                        R.REAMOUNT,
                        R.RESEQ,
                        R.REMAXAMT,
                        R.INDATEM,
                        R.REALSCORE,
                        R.REALPOINT,
                        R.RELASTDATE,
                        R.REALGUBUN,
                        R.PDFFILENAME,
                        S.USERID AS Suserid,
                        S.REQDATE AS Sreqdate,
                        S.REALID AS Srealid,
                        U.USERNM AS USERNM,
                        RS.UniqueNo,
                        RS.Gubun,
                        RS.Address,
                        RS.PrintDate,
                        RA.RankNo AS RankNo_a,
                        RA.RgsAimCont AS RgsAimCont_a,
                        RA.Receve AS Receve_a,
                        RA.RgsCaus AS RgsCaus_a,
                        RA.NomprsAndEtc AS NomprsAndEtc_a,
                        RB.RankNo AS RankNo_b,
                        RB.RgsAimCont AS RgsAimCont_b,
                        RB.Receve AS Receve_b,
                        RB.RgsCaus AS RgsCaus_b,
                        RB.NomprsAndEtc AS NomprsAndEtc_b,
                        ROW_NUMBER() OVER (PARTITION BY R.REALID ORDER BY S.REQDATE DESC) AS rn
                    FROM TB_REALINFO R
                    INNER JOIN SEARCH_CTE S
                        ON R.REALID = S.REALID
                        AND S.rn = 1
                    INNER JOIN TB_USERINFO U
                        ON S.USERID = U.USERID
                    LEFT JOIN TB_REALSUMMARY RS
                        ON R.REALID = RS.REALID
                    LEFT JOIN (
                        SELECT DISTINCT REALID, RankNo, RgsAimCont, Receve, RgsCaus, NomprsAndEtc
                        FROM TB_REALAOWN
                    ) RA ON R.REALID = RA.REALID
                    LEFT JOIN (
                        SELECT DISTINCT REALID, RankNo, RgsAimCont, Receve, RgsCaus, NomprsAndEtc
                        FROM TB_REALBOWN
                    ) RB ON R.REALID = RB.REALID
                    LEFT JOIN COUNT_CTE C ON R.REALID = C.REALID
                    WHERE S.REQDATE BETWEEN
                        CAST(:startDate AS DATETIME)
                        AND DATEADD(MILLISECOND, -1, DATEADD(DAY, 1, CAST(:endDate AS DATETIME)))
                )
                SELECT
                    CTE.REALID,
                    CTE.USERID,
                    CTE.REQDATE,
                    CTE.REALADD,
                    CTE.REGDATE,
                    CTE.RESIDO,
                    CTE.REGUGUN,
                    CTE.REMOK,
                    CTE.REWON,
                    CTE.REWONDATE,
                    CTE.REJIMOK,
                    CTE.REAREA,
                    CTE.REAMOUNT,
                    CTE.RESEQ,
                    CTE.REMAXAMT,
                    CTE.INDATEM,
                    CTE.REALSCORE,
                    CTE.REALPOINT,
                    CTE.RELASTDATE,
                    CTE.REALGUBUN,
                    CTE.PDFFILENAME,
                    CTE.Suserid,
                    CTE.Sreqdate,
                    CTE.Srealid,
                    CTE.USERNM,
                    CTE.UniqueNo,
                    CTE.Gubun,
                    CTE.Address,
                    CTE.PrintDate,
                    CASE
                        WHEN CTE.UniqueNo IS NOT NULL THEN COALESCE((
                            SELECT DISTINCT RankNo_a, RgsAimCont_a, Receve_a, RgsCaus_a, NomprsAndEtc_a
                            FROM CTE
                            WHERE CTE.RankNo_a IS NOT NULL AND CTE.UniqueNo IS NOT NULL
                            FOR JSON PATH
                        ), '[]')
                        ELSE '[]'
                    END AS RankNo_a_data,
                    CASE
                        WHEN CTE.UniqueNo IS NOT NULL THEN COALESCE((
                            SELECT DISTINCT RankNo_b, RgsAimCont_b, Receve_b, RgsCaus_b, NomprsAndEtc_b
                            FROM CTE
                            WHERE CTE.RankNo_b IS NOT NULL AND CTE.UniqueNo IS NOT NULL
                            FOR JSON PATH
                        ), '[]')
                        ELSE '[]'
                    END AS RankNo_b_data,
                    C.record_count,
                    CTE.rn
                FROM CTE
                LEFT JOIN COUNT_CTE C ON CTE.REALID = C.REALID
                WHERE CTE.rn = 1
                ORDER BY C.record_count DESC, CTE.REQDATE DESC;
                """;

        List<Map<String, Object>> items = this.sqlRunner.getRows(sql.toString(), dicParam);

        // RankNo_a_data와 RankNo_b_data가 null인 경우 빈 배열 문자열 "[]"로 설정 (중요)
        for (Map<String, Object> item : items) {
            if (item.get("RankNo_a_data") == null) {
                item.put("RankNo_a_data", "[]"); // 빈 JSON 배열 문자열 할당
            }
            if (item.get("RankNo_b_data") == null) {
                item.put("RankNo_b_data", "[]"); // 빈 JSON 배열 문자열 할당
            }
        }

        return items;
    }


    public List<Map<String, Object>> getmostData(String startDate, String endDate, String property) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);

        String sql = """
                WITH SEARCH_CTE AS (
                    SELECT
                        S.REALID,
                        S.USERID,
                        S.REQDATE,
                        ROW_NUMBER() OVER (PARTITION BY S.REALID ORDER BY S.REQDATE DESC) AS rn
                    FROM TB_SEARCHINFO S
                ),
                COUNT_CTE AS (
                    SELECT
                        REALID,
                        COUNT(*) AS record_count
                    FROM TB_SEARCHINFO
                    GROUP BY REALID
                ),
                CTE AS (
                    SELECT
                        R.REALID,
                        R.USERID,
                        R.REQDATE,
                        R.REALADD,
                        R.REGDATE,
                        R.RESIDO,
                        R.REGUGUN,
                        R.REMOK,
                        R.REWON,
                        R.REWONDATE,
                        R.REJIMOK,
                        R.REAREA,
                        R.REAMOUNT,
                        R.RESEQ,
                        R.REMAXAMT,
                        R.INDATEM,
                        R.REALSCORE,
                        R.REALPOINT,
                        R.RELASTDATE,
                        R.REALGUBUN,
                        R.PDFFILENAME,
                        S.USERID AS Suserid,
                        S.REQDATE AS Sreqdate,
                        S.REALID AS Srealid,
                        U.USERNM AS USERNM,
                        RS.UniqueNo,
                        RS.Gubun,
                        RS.Address,
                        RS.PrintDate,
                        RA.RankNo AS RankNo_a,
                        RA.RgsAimCont AS RgsAimCont_a,
                        RA.Receve AS Receve_a,
                        RA.RgsCaus AS RgsCaus_a,
                        RA.NomprsAndEtc AS NomprsAndEtc_a,
                        RB.RankNo AS RankNo_b,
                        RB.RgsAimCont AS RgsAimCont_b,
                        RB.Receve AS Receve_b,
                        RB.RgsCaus AS RgsCaus_b,
                        RB.NomprsAndEtc AS NomprsAndEtc_b,
                        ROW_NUMBER() OVER (PARTITION BY R.REALID ORDER BY S.REQDATE DESC) AS rn,
                        C.record_count  -- 추가: record_count를 CTE에 포함시킴
                    FROM TB_REALINFO R
                    INNER JOIN SEARCH_CTE S
                        ON R.REALID = S.REALID
                        AND S.rn = 1
                    INNER JOIN TB_USERINFO U
                        ON S.USERID = U.USERID
                    LEFT JOIN TB_REALSUMMARY RS
                        ON R.REALID = RS.REALID
                    LEFT JOIN (
                        SELECT DISTINCT REALID, RankNo, RgsAimCont, Receve, RgsCaus, NomprsAndEtc
                        FROM TB_REALAOWN
                    ) RA ON R.REALID = RA.REALID
                    LEFT JOIN (
                        SELECT DISTINCT REALID, RankNo, RgsAimCont, Receve, RgsCaus, NomprsAndEtc
                        FROM TB_REALBOWN
                    ) RB ON R.REALID = RB.REALID
                    LEFT JOIN COUNT_CTE C ON R.REALID = C.REALID  -- COUNT_CTE와 조인
                    WHERE S.REQDATE BETWEEN
                        CAST(:startDate AS DATETIME)
                        AND DATEADD(MILLISECOND, -1, DATEADD(DAY, 1, CAST(:endDate AS DATETIME)))
                )
                SELECT
                    CTE.REALID,
                    CTE.USERID,
                    CTE.REQDATE,
                    CTE.REALADD,
                    CTE.REGDATE,
                    CTE.RESIDO,
                    CTE.REGUGUN,
                    CTE.REMOK,
                    CTE.REWON,
                    CTE.REWONDATE,
                    CTE.REJIMOK,
                    CTE.REAREA,
                    CTE.REAMOUNT,
                    CTE.RESEQ,
                    CTE.REMAXAMT,
                    CTE.INDATEM,
                    CTE.REALSCORE,
                    CTE.REALPOINT,
                    CTE.RELASTDATE,
                    CTE.REALGUBUN,
                    CTE.PDFFILENAME,
                    CTE.Suserid,
                    CTE.Sreqdate,
                    CTE.Srealid,
                    CTE.USERNM,
                    CTE.UniqueNo,
                    CTE.Gubun,
                    CTE.Address,
                    CTE.PrintDate,
                    CASE
                        WHEN CTE.UniqueNo IS NOT NULL THEN COALESCE((
                            SELECT DISTINCT RankNo_a, RgsAimCont_a, Receve_a, RgsCaus_a, NomprsAndEtc_a
                            FROM CTE
                            WHERE CTE.RankNo_a IS NOT NULL AND CTE.UniqueNo IS NOT NULL
                            FOR JSON PATH
                        ), '[]')
                        ELSE '[]'
                    END AS RankNo_a_data,
                    CASE
                        WHEN CTE.UniqueNo IS NOT NULL THEN COALESCE((
                            SELECT DISTINCT RankNo_b, RgsAimCont_b, Receve_b, RgsCaus_b, NomprsAndEtc_b
                            FROM CTE
                            WHERE CTE.RankNo_b IS NOT NULL AND CTE.UniqueNo IS NOT NULL
                            FOR JSON PATH
                        ), '[]')
                        ELSE '[]'
                    END AS RankNo_b_data,
                    CTE.record_count,
                    CTE.rn
                FROM CTE
                WHERE CTE.rn = 1
                ORDER BY CTE.record_count DESC;
                """;

        List<Map<String, Object>> items = this.sqlRunner.getRows(sql.toString(), dicParam);

        // RankNo_a_data와 RankNo_b_data가 null인 경우 빈 배열 문자열 "[]"로 설정 (중요)
        for (Map<String, Object> item : items) {
            if (item.get("RankNo_a_data") == null) {
                item.put("RankNo_a_data", "[]"); // 빈 JSON 배열 문자열 할당
            }
            if (item.get("RankNo_b_data") == null) {
                item.put("RankNo_b_data", "[]"); // 빈 JSON 배열 문자열 할당
            }
        }

        return items;
    }


    public List<Map<String, Object>> getpopularData(String startDate, String endDate, String property) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);

        String sql = """
                WITH SEARCH_CTE AS (
                    SELECT
                        S.REALID,
                        S.USERID,
                        S.REQDATE,
                        ROW_NUMBER() OVER (PARTITION BY S.REALID ORDER BY S.REQDATE DESC) AS rn
                    FROM TB_SEARCHINFO S
                ),
                COUNT_CTE AS (
                    SELECT
                        REALID,
                        COUNT(*) AS record_count
                    FROM TB_SEARCHINFO
                    GROUP BY REALID
                ),
                CTE AS (
                    SELECT
                        R.REALID,
                        R.USERID,
                        R.REQDATE,
                        R.REALADD,
                        R.REGDATE,
                        R.RESIDO,
                        R.REGUGUN,
                        R.REMOK,
                        R.REWON,
                        R.REWONDATE,
                        R.REJIMOK,
                        R.REAREA,
                        R.REAMOUNT,
                        R.RESEQ,
                        R.REMAXAMT,
                        R.INDATEM,
                        R.REALSCORE,
                        R.REALPOINT,
                        R.RELASTDATE,
                        R.REALGUBUN,
                        R.PDFFILENAME,
                        S.USERID AS Suserid,
                        S.REQDATE AS Sreqdate,
                        S.REALID AS Srealid,
                        U.USERNM AS USERNM,
                        RS.UniqueNo,
                        RS.Gubun,
                        RS.Address,
                        RS.PrintDate,
                        RA.RankNo AS RankNo_a,
                        RA.RgsAimCont AS RgsAimCont_a,
                        RA.Receve AS Receve_a,
                        RA.RgsCaus AS RgsCaus_a,
                        RA.NomprsAndEtc AS NomprsAndEtc_a,
                        RB.RankNo AS RankNo_b,
                        RB.RgsAimCont AS RgsAimCont_b,
                        RB.Receve AS Receve_b,
                        RB.RgsCaus AS RgsCaus_b,
                        RB.NomprsAndEtc AS NomprsAndEtc_b,
                        ROW_NUMBER() OVER (PARTITION BY R.REALID ORDER BY S.REQDATE DESC) AS rn,
                        C.record_count  -- 추가: record_count를 CTE에 포함시킴
                    FROM TB_REALINFO R
                    INNER JOIN SEARCH_CTE S
                        ON R.REALID = S.REALID
                        AND S.rn = 1
                    INNER JOIN TB_USERINFO U
                        ON S.USERID = U.USERID
                    LEFT JOIN TB_REALSUMMARY RS
                        ON R.REALID = RS.REALID
                    LEFT JOIN (
                        SELECT DISTINCT REALID, RankNo, RgsAimCont, Receve, RgsCaus, NomprsAndEtc
                        FROM TB_REALAOWN
                    ) RA ON R.REALID = RA.REALID
                    LEFT JOIN (
                        SELECT DISTINCT REALID, RankNo, RgsAimCont, Receve, RgsCaus, NomprsAndEtc
                        FROM TB_REALBOWN
                    ) RB ON R.REALID = RB.REALID
                    LEFT JOIN COUNT_CTE C ON R.REALID = C.REALID  -- COUNT_CTE와 조인
                    WHERE S.REQDATE BETWEEN
                        CAST(:startDate AS DATETIME)
                        AND DATEADD(MILLISECOND, -1, DATEADD(DAY, 1, CAST(:endDate AS DATETIME)))
                )
                SELECT
                    CTE.REALID,
                    CTE.USERID,
                    CTE.REQDATE,
                    CTE.REALADD,
                    CTE.REGDATE,
                    CTE.RESIDO,
                    CTE.REGUGUN,
                    CTE.REMOK,
                    CTE.REWON,
                    CTE.REWONDATE,
                    CTE.REJIMOK,
                    CTE.REAREA,
                    CTE.REAMOUNT,
                    CTE.RESEQ,
                    CTE.REMAXAMT,
                    CTE.INDATEM,
                    CTE.REALSCORE,
                    CTE.REALPOINT,
                    CTE.RELASTDATE,
                    CTE.REALGUBUN,
                    CTE.PDFFILENAME,
                    CTE.Suserid,
                    CTE.Sreqdate,
                    CTE.Srealid,
                    CTE.USERNM,
                    CTE.UniqueNo,
                    CTE.Gubun,
                    CTE.Address,
                    CTE.PrintDate,
                    CASE
                        WHEN CTE.UniqueNo IS NOT NULL THEN COALESCE((
                            SELECT DISTINCT RankNo_a, RgsAimCont_a, Receve_a, RgsCaus_a, NomprsAndEtc_a
                            FROM CTE
                            WHERE CTE.RankNo_a IS NOT NULL AND CTE.UniqueNo IS NOT NULL
                            FOR JSON PATH
                        ), '[]')
                        ELSE '[]'
                    END AS RankNo_a_data,
                    CASE
                        WHEN CTE.UniqueNo IS NOT NULL THEN COALESCE((
                            SELECT DISTINCT RankNo_b, RgsAimCont_b, Receve_b, RgsCaus_b, NomprsAndEtc_b
                            FROM CTE
                            WHERE CTE.RankNo_b IS NOT NULL AND CTE.UniqueNo IS NOT NULL
                            FOR JSON PATH
                        ), '[]')
                        ELSE '[]'
                    END AS RankNo_b_data,
                    CTE.record_count,
                    CTE.rn
                FROM CTE
                WHERE CTE.rn = 1
                ORDER BY CTE.record_count DESC;
                """;

        List<Map<String, Object>> items = this.sqlRunner.getRows(sql.toString(), dicParam);

        // RankNo_a_data와 RankNo_b_data가 null인 경우 빈 배열 문자열 "[]"로 설정 (중요)
        for (Map<String, Object> item : items) {
            if (item.get("RankNo_a_data") == null) {
                item.put("RankNo_a_data", "[]"); // 빈 JSON 배열 문자열 할당
            }
            if (item.get("RankNo_b_data") == null) {
                item.put("RankNo_b_data", "[]"); // 빈 JSON 배열 문자열 할당
            }
        }

        return items;
    }


    public List<Map<String,Object>> getSeachList(String userid){
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("userid", userid);
        System.out.println("원형점수관리에서 userid 확인"+userid);

        String sql = """
                WITH CTE AS (
                    SELECT R.*,
                           S.USERID as Suserid,
                           S.REQDATE as Sreqdate,
                           S.REALID as Srealid,
                           ROW_NUMBER() OVER (PARTITION BY R.REALID ORDER BY S.REQDATE) AS row_num
                    FROM MOB_FACTCHK.dbo.TB_REALINFO R
                    LEFT JOIN MOB_FACTCHK.dbo.TB_SEARCHINFO S
                    ON R.REALID = S.REALID
                    WHERE S.USERID = :userid
                )
                SELECT *
                FROM CTE
                WHERE row_num = 1
                """;

        List<Map<String, Object>> items = this.sqlRunner.getRows(sql.toString(), dicParam);
        return items;
    }

}
