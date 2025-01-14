package mes.app.dashboard.service;

import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FactCheckDashBoardService {

    @Autowired
    SqlRunner sqlRunner;
    // 금일 신규 사용자
    public List<Map<String, Object>> todaySignup() {

        String sql = """
            SELECT
                INDATEM,
                USERNM,
                USERADDR,
                AGENUM,
                USERHP
            FROM
                TB_USERINFO
            WHERE
                CONVERT(DATE, INDATEM) = CONVERT(DATE, GETDATE());
            """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
    // 금일 상위 검색지역
    public List<Map<String, Object>> todayAddress() {

        String sql = """
                SELECT
                    r.REALID,
                    r.USERID,
                    s.REQDATE,
                    r.REALADD,
                    r.REGDATE,
                    r.RESIDO,
                    r.REGUGUN,
                    r.REMOK,
                    r.REWON,
                    r.REWONDATE,
                    r.REJIMOK,
                    r.REAREA,
                    r.REAMOUNT,
                    r.RESEQ,
                    r.REMAXAMT,
                    r.INDATEM,
                    r.REALSCORE,
                    r.REALPOINT,
                    r.RELASTDATE,
                    COUNT(s.REALID) AS VIEW_COUNT
                FROM
                    TB_REALINFO r
                LEFT JOIN
                    TB_SEARCHINFO s ON r.REALID = s.REALID
                WHERE
                    CAST(s.REQDATE AS DATE) = CAST(GETDATE() AS DATE)
                GROUP BY
                    r.REALID, r.USERID, s.REQDATE, r.REALADD, r.REGDATE,
                    r.RESIDO, r.REGUGUN, r.REMOK, r.REWON, r.REWONDATE,
                    r.REJIMOK, r.REAREA, r.REAMOUNT, r.RESEQ, r.REMAXAMT,
                    r.INDATEM, r.REALSCORE, r.REALPOINT, r.RELASTDATE
                ORDER BY
                    VIEW_COUNT DESC;
            """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
    // 1:1 미답변 문의글
    public List<Map<String, Object>> QnAList() {

        String sql = """
            SELECT
                Q1.QSTSEQ,
                Q1.USERID,
                Q1.QSTTEXT,
                Q1.QSTTEL,
                Q1.INDATEM
            FROM
                TB_USERQST Q1
            WHERE
                Q1.FLAG = '0'
                AND NOT EXISTS (
                    SELECT 1
                    FROM TB_USERQST Q2
                    WHERE Q2.FLAG = '1'
                      AND Q2.CHSEQ = Q1.QSTSEQ
                )
            ORDER BY
                Q1.INDATEM ASC;
            """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
    // 차트데이터(일주일 가입자현황)
    public List<Map<String, Object>> SignupOfWeek() {

        String sql = """
                SELECT
                    CAST(INDATEM AS DATE) AS DATE,
                    DATENAME(WEEKDAY, INDATEM) AS DAY,
                    COUNT(*) AS CNT
                FROM
                    TB_USERINFO
                WHERE
                    INDATEM >= CAST(GETDATE() - 6 AS DATE)
                    AND INDATEM < CAST(GETDATE() + 1 AS DATE)
                GROUP BY
                    CAST(INDATEM AS DATE),
                    DATENAME(WEEKDAY, INDATEM)
                ORDER BY
                    DATE;
            """;
        // 데이터가 없는 요일 조회 예시
//        WITH DateRange AS (
//                SELECT CAST(GETDATE() - 6 AS DATE) + ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) - 1 AS INDATEM
//                FROM master..spt_values
//        WHERE type = 'P' AND number < 7
//              )
//        SELECT
//        DR.INDATEM AS DATE,
//                DATENAME(WEEKDAY, DR.INDATEM) AS DAY,
//        COUNT(UI.INDATEM) AS CNT
//        FROM
//        DateRange DR
//        LEFT JOIN
//        TB_USERINFO UI ON CAST(UI.INDATEM AS DATE) = DR.INDATEM
//        GROUP BY
//        DR.INDATEM,
//                DATENAME(WEEKDAY, DR.INDATEM)
//        ORDER BY
//        DR.INDATEM;


        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
    // 차트데이터(유출자현황)
    public List<Map<String, Object>> Signout() {

        String sql = """
            SELECT
                CAST(CONVERT(DATE, STUFF(STUFF(WDRAWDATE, 5, 0, '-'), 8, 0, '-')) AS DATE) AS DATE,
                DATENAME(WEEKDAY, CAST(CONVERT(DATE, STUFF(STUFF(WDRAWDATE, 5, 0, '-'), 8, 0, '-')) AS DATE)) AS DAY,
                COUNT(*) AS CNT
            FROM
                TB_USERINFO
            WHERE
                CAST(CONVERT(DATE, STUFF(STUFF(WDRAWDATE, 5, 0, '-'), 8, 0, '-')) AS DATE) >= CAST(GETDATE() - 6 AS DATE)
                AND CAST(CONVERT(DATE, STUFF(STUFF(WDRAWDATE, 5, 0, '-'), 8, 0, '-')) AS DATE) < CAST(GETDATE() + 1 AS DATE)
            GROUP BY
                CAST(CONVERT(DATE, STUFF(STUFF(WDRAWDATE, 5, 0, '-'), 8, 0, '-')) AS DATE),
                DATENAME(WEEKDAY, CAST(CONVERT(DATE, STUFF(STUFF(WDRAWDATE, 5, 0, '-'), 8, 0, '-')) AS DATE))
            ORDER BY
                DATE;
            """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
    // 차트데이터(등기열람건수)
    public List<Map<String, Object>> CntOfRegister() {

        String sql = """
                SELECT
                    CAST(REQDATE AS DATE) AS DATE,
                    DATENAME(WEEKDAY, REQDATE) AS DAY,
                    COUNT(*) AS CNT
                FROM
                    MOB_FACTCHK.dbo.TB_SEARCHINFO
                WHERE
                    REQDATE >= CAST(GETDATE() - 6 AS DATE)
                    AND REQDATE < CAST(GETDATE() + 1 AS DATE)
                GROUP BY
                    CAST(REQDATE AS DATE),
                    DATENAME(WEEKDAY, REQDATE)
                ORDER BY
                    DATE;
            """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
    // 차트데이터(상위검색지역)
    public List<Map<String, Object>> BestSearch() {

        String sql = """
            """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
    // 카드 데이터(집계 총가입자, 미답변 문의건수, 금일 상위 검색지역)
    public List<Map<String, Object>> cardDatas() {

        String sql = """
            SELECT
                SUM(CASE WHEN CONVERT(DATE, INDATEM) = CONVERT(DATE, GETDATE()) THEN 1 ELSE 0 END) AS TODAY_COUNT,
                SUM(CASE WHEN CONVERT(DATE, INDATEM) = CONVERT(DATE, DATEADD(DAY, -1, GETDATE())) THEN 1 ELSE 0 END) AS YESTERDAY_COUNT
            FROM
                TB_USERINFO;
            """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
}
