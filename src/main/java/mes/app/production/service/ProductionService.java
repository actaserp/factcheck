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

    public Map<String, Object> getlist(String startDate, String endDate, String property, String username,String keyword) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);
        dicParam.addValue("userid", username);
        Map<String, Object> result = new HashMap<>();

        result.put("my_property", searchDataList(startDate, endDate, "my_property", username,keyword));
        result.put("recent_property", searchDataList(startDate, endDate, "recent_property", username,keyword));
        result.put("most_viewed_property", searchDataList(startDate, endDate, "most_viewed_property", username,keyword));
        result.put("popular_property", searchDataList(startDate, endDate, "popular_property", username,keyword));

        return result;
    }

    private List<Map<String, Object>> searchDataList(String startDate, String endDate, String property, String userid,String keyword) {
        // searchType에 따라 다른 쿼리 호출
        switch (property) {
            case "my_property":
                return getmyData(startDate, endDate, property, userid,keyword);
            case "recent_property":
                return getrecentData(startDate, endDate, property,keyword);
            case "most_viewed_property":
                return getmostData(startDate, endDate, property,keyword);
            case "popular_property":
                return getpopularData(startDate, endDate, property,keyword);
            default:
                return Collections.emptyList();
        }
    }


    public List<Map<String, Object>> getmyData(String startDate, String endDate, String property, String username, String keyword) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();

        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);
        dicParam.addValue("userid", username);

        System.out.println("쿼리 들어왔을때 "+startDate+endDate);

        if (keyword != null && !keyword.trim().isEmpty()) {
            dicParam.addValue("keyword", "%" + keyword + "%");
        } else {
            dicParam.addValue("keyword", null);
        }

        // 기본 쿼리 구성
        StringBuilder sql = new StringBuilder("""
        WITH CTE AS (
            SELECT
                R.REALID, R.USERID, R.REQDATE, R.REALADD, R.REGDATE,
                R.RESIDO, R.REGUGUN, R.REMOK, R.REWON, R.REWONDATE,
                R.REJIMOK, R.REAREA, R.REAMOUNT, R.RESEQ, R.REMAXAMT,
                R.INDATEM, R.REALSCORE, R.REALPOINT, R.RELASTDATE, R.REALGUBUN,
                R.PDFFILENAME, S.USERID AS Suserid, S.REQDATE AS Sreqdate, S.REALID AS Srealid,
                RS.UniqueNo, RS.Gubun, RS.Address, RS.PrintDate,
                RA.RankNo AS RankNo_a, RA.RgsAimCont AS RgsAimCont_a, RA.Receve AS Receve_a,
                RA.RgsCaus AS RgsCaus_a, RA.NomprsAndEtc AS NomprsAndEtc_a,
                RB.RankNo AS RankNo_b, RB.RgsAimCont AS RgsAimCont_b, RB.Receve AS Receve_b,
                RB.RgsCaus AS RgsCaus_b, RB.NomprsAndEtc AS NomprsAndEtc_b,
                U.USERNM,
                ROW_NUMBER() OVER (PARTITION BY R.REALID ORDER BY S.REQDATE DESC) AS rn
            FROM TB_REALINFO R
            INNER JOIN TB_SEARCHINFO S ON R.REALID = S.REALID
            LEFT JOIN TB_USERINFO U ON S.USERID = U.USERID
            LEFT JOIN TB_REALSUMMARY RS ON R.REALID = RS.REALID
            LEFT JOIN TB_REALAOWN RA ON R.REALID = RA.REALID
            LEFT JOIN TB_REALBOWN RB ON R.REALID = RB.REALID
            LEFT JOIN auth_user AU ON S.USERID = AU.username
            WHERE S.REALID = R.REALID
            AND S.REQDATE BETWEEN CAST(:startDate AS DATETIME) AND DATEADD(SECOND, 86399, CAST(:endDate AS DATETIME))
            AND (:keyword IS NULL OR R.REALADD LIKE '%' + :keyword + '%')
    """);

        // username이 'super' 또는 'admin'이면 S.USERID IN ('super', 'admin') 조건 추가
        if ("super".equals(username) || "admin".equals(username)) {
            sql.append(" AND S.USERID IN ('super', 'admin')");
        } else {
            sql.append(" AND S.USERID = :userid");
        }

        // CTE에서 결과 조회
        sql.append("""
        )
        SELECT CTE.REALID, CTE.USERID, CTE.REQDATE, CTE.REALADD, CTE.REGDATE,
            CTE.RESIDO, CTE.REGUGUN, CTE.REMOK, CTE.REWON, CTE.REWONDATE,
            CTE.REJIMOK, CTE.REAREA, CTE.REAMOUNT, CTE.RESEQ, CTE.REMAXAMT,
            CTE.INDATEM, CTE.REALSCORE, CTE.REALPOINT, CTE.RELASTDATE, CTE.REALGUBUN,
            CTE.PDFFILENAME, CTE.Suserid, CTE.Sreqdate, CTE.Srealid, CTE.UniqueNo,
            CTE.Gubun, CTE.Address, CTE.PrintDate, CTE.USERNM,
            CASE WHEN CTE.UniqueNo IS NOT NULL THEN COALESCE((
                SELECT DISTINCT RankNo_a, RgsAimCont_a, Receve_a, RgsCaus_a, NomprsAndEtc_a
                FROM CTE
                WHERE CTE.RankNo_a IS NOT NULL AND CTE.UniqueNo IS NOT NULL
                FOR JSON PATH
            ), '[]') ELSE '[]' END AS RankNo_a_data,
            CASE WHEN CTE.UniqueNo IS NOT NULL THEN COALESCE((
                SELECT DISTINCT RankNo_b, RgsAimCont_b, Receve_b, RgsCaus_b, NomprsAndEtc_b
                FROM CTE
                WHERE CTE.RankNo_b IS NOT NULL AND CTE.UniqueNo IS NOT NULL
                FOR JSON PATH
            ), '[]') ELSE '[]' END AS RankNo_b_data,
            CTE.rn
        FROM CTE
        WHERE CTE.rn = 1
        ORDER BY CTE.REQDATE DESC;
    """);

        List<Map<String, Object>> items = this.sqlRunner.getRows(sql.toString(), dicParam);

        // RankNo_a_data와 RankNo_b_data가 null이면 빈 배열 문자열 "[]"로 설정
        for (Map<String, Object> item : items) {
            if (item.get("RankNo_a_data") == null) {
                item.put("RankNo_a_data", "[]");
            }
            if (item.get("RankNo_b_data") == null) {
                item.put("RankNo_b_data", "[]");
            }
        }

        return items;
    }


    public List<Map<String, Object>> getrecentData(String startDate, String endDate, String property, String keyword) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);
        if (keyword !=null && !keyword.trim().isEmpty()){
            dicParam.addValue("keyword","%"+keyword+"%");
        } else {
            dicParam.addValue("keyword",null);
        }

        String sql = """
                WITH SEARCH_CTE AS (
                    SELECT
                        S.REALID,
                        S.USERID,
                        S.REQDATE,
                        ROW_NUMBER() OVER (PARTITION BY S.REALID ORDER BY S.REQDATE DESC) AS rn
                    FROM TB_SEARCHINFO S
                    LEFT JOIN auth_user AU ON S.USERID = AU.username
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
                    LEFT JOIN TB_USERINFO U
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
                          AND (:keyword IS NULL OR R.REALADD LIKE '%' + :keyword + '%')
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


    public List<Map<String, Object>> getmostData(String startDate, String endDate, String property, String keyword) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);
        if (keyword !=null && !keyword.trim().isEmpty()){
            dicParam.addValue("keyword","%"+keyword+"%");
        } else {
            dicParam.addValue("keyword",null);
        }

        String sql = """
                WITH PinNo_Count_CTE AS (
                       SELECT
                           REALID,
                           PinNo,
                           COUNT(*) OVER (PARTITION BY PinNo) AS PinNo_Count,
                           ROW_NUMBER() OVER (PARTITION BY PinNo ORDER BY REALID DESC) AS rn
                       FROM TB_REALINFOXML
                   ),
                CTE AS (
                    SELECT
                        RA.REALID,
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
                        CONCAT(RA.REALID, RB.REALID) AS UniqueNo
                    FROM (
                        SELECT DISTINCT REALID, RankNo, RgsAimCont, Receve, RgsCaus, NomprsAndEtc
                        FROM TB_REALAOWN
                    ) RA
                    LEFT JOIN (
                        SELECT DISTINCT REALID, RankNo, RgsAimCont, Receve, RgsCaus, NomprsAndEtc
                        FROM TB_REALBOWN
                    ) RB
                    ON RA.REALID = RB.REALID
                ),
                RankedRecords AS (
                    SELECT
                        RX.REALID,
                        RX.PinNo,
                        PNC.PinNo_Count,
                        MAX(R.USERID) AS USERID,
                        MAX(R.REQDATE) AS REQDATE,
                        MAX(R.REALADD) AS REALADD,
                        MAX(R.REGDATE) AS REGDATE,
                        MAX(R.RESIDO) AS RESIDO,
                        MAX(R.REGUGUN) AS REGUGUN,
                        MAX(R.REMOK) AS REMOK,
                        MAX(R.REWON) AS REWON,
                        MAX(R.REWONDATE) AS REWONDATE,
                        MAX(R.REJIMOK) AS REJIMOK,
                        MAX(R.REAREA) AS REAREA,
                        MAX(R.REAMOUNT) AS REAMOUNT,
                        MAX(R.RESEQ) AS RESEQ,
                        MAX(R.REMAXAMT) AS REMAXAMT,
                        MAX(R.INDATEM) AS INDATEM,
                        MAX(R.REALSCORE) AS REALSCORE,
                        MAX(R.REALPOINT) AS REALPOINT,
                        MAX(R.RELASTDATE) AS REALASTDATE,
                        MAX(R.REALGUBUN) AS REALGUBUN,
                        MAX(RX.REALID) AS RXREALID,
                        MAX(RX.WksbiBalDate) AS WksbiBalDate,
                        MAX(RX.WksbiBalNoTime) AS WksbiBalNoTime,
                        S.Gubun,
                        S.Address,
                        S.PrintDate,
                        (
                            SELECT
                                RankNo_a, RgsAimCont_a, Receve_a, RgsCaus_a, NomprsAndEtc_a
                            FROM CTE
                            WHERE CTE.REALID = R.REALID
                            FOR JSON PATH
                        ) AS RankNo_a_data,
                        (
                            SELECT
                                RankNo_b, RgsAimCont_b, Receve_b, RgsCaus_b, NomprsAndEtc_b
                            FROM CTE
                            WHERE CTE.REALID = R.REALID
                            FOR JSON PATH
                        ) AS RankNo_b_data,
                        ROW_NUMBER() OVER (PARTITION BY RX.PinNo, PNC.PinNo_Count ORDER BY RX.WksbiBalDate DESC) AS RowNum
                    FROM
                        TB_REALINFO R
                    LEFT JOIN (
                        SELECT DISTINCT REALID, RankNo, RgsAimCont, Receve, RgsCaus, NomprsAndEtc
                        FROM TB_REALAOWN
                    ) RA ON R.REALID = RA.REALID
                    LEFT JOIN (
                        SELECT DISTINCT REALID, RankNo, RgsAimCont, Receve, RgsCaus, NomprsAndEtc
                        FROM TB_REALBOWN
                    ) RB ON R.REALID = RB.REALID
                    JOIN
                        TB_REALINFOXML RX
                        ON R.REALID = RX.REALID
                    LEFT JOIN
                        TB_REALSUMMARY S
                        ON R.REALID = S.REALID
                    LEFT JOIN CTE
                        ON R.REALID = CTE.REALID
                    LEFT JOIN PinNo_Count_CTE PNC
                        ON RX.PinNo = PNC.PinNo
                    WHERE REPLACE(RX.WksbiBalDate, '/', '-') BETWEEN :startDate AND :endDate
                     AND (:keyword IS NULL OR R.REALADD LIKE '%' + :keyword + '%')
                    GROUP BY
                        RX.PinNo, S.REALID, S.Gubun, S.Address, S.PrintDate, PNC.PinNo_Count, RX.WksbiBalDate,R.REALID,RX.REALID
                )
                SELECT *
                FROM RankedRecords
                WHERE RowNum = 1
                ORDER BY
                    PinNo_Count DESC,
                    PinNo
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


    public List<Map<String, Object>> getpopularData(String startDate, String endDate, String property, String keyword) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);
        if (keyword !=null && !keyword.trim().isEmpty()){
            dicParam.addValue("keyword","%"+keyword+"%");
        } else {
            dicParam.addValue("keyword",null);
        }

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
                        C.record_count
                    FROM TB_REALINFO R
                    INNER JOIN SEARCH_CTE S
                        ON R.REALID = S.REALID
                        AND S.rn = 1
                   LEFT JOIN TB_USERINFO U
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
                          AND (:keyword IS NULL OR R.REALADD LIKE '%' + :keyword + '%')
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


    public List<Map<String,Object>> getSeachList(String userid, String keyword){
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("userid", userid);
        if (keyword !=null && !keyword.trim().isEmpty()){
            dicParam.addValue("keyword","%"+keyword+"%");
        } else {
            dicParam.addValue("keyword",null);
        }

        String sql = """
                WITH CTE AS (
                    SELECT R.*,
                           S.USERID as Suserid,
                           S.REQDATE as Sreqdate,
                           S.REALID as Srealid,
                           ROW_NUMBER() OVER (PARTITION BY R.REALID ORDER BY S.REQDATE) AS row_num
                    FROM TB_REALINFO R
                    LEFT JOIN TB_SEARCHINFO S
                    ON R.REALID = S.REALID
                    WHERE S.USERID = :userid
                    AND (:keyword IS NULL OR R.REALADD LIKE '%' + :keyword + '%')
                )
                SELECT *
                FROM CTE
                WHERE row_num = 1
                """;

        List<Map<String, Object>> items = this.sqlRunner.getRows(sql.toString(), dicParam);
        return items;
    }

}
