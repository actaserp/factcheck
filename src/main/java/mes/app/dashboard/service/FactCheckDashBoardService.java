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
    // 금일 상위 검색매물
    public List<Map<String, Object>> todayAddress() {

        String sql = """
                WITH FirstRealInfo AS (
                    SELECT
                        x.PinNo,
                        MIN(r.REALID) AS REALID
                    FROM
                        TB_REALINFOXML x
                    LEFT JOIN
                        TB_REALINFO r ON x.REALID = r.REALID
                    WHERE
                        r.INDATEM >= CAST(GETDATE() AS DATE)
                        AND r.INDATEM < DATEADD(DAY, 1, CAST(GETDATE() AS DATE)) 
                    GROUP BY
                        x.PinNo
                )
                SELECT
                    f.PinNo,
                    r.REALID,
                    r.USERID,
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
                    COUNT(x.PinNo) AS VIEW_COUNT
                FROM
                    FirstRealInfo f
                LEFT JOIN
                    TB_REALINFO r ON f.REALID = r.REALID  -- 가장 먼저 조회된 REALID에 해당하는 데이터만 가져옴
                LEFT JOIN
                    TB_REALINFOXML x ON f.PinNo = x.PinNo  -- VIEW_COUNT 계산을 위해 조인
                GROUP BY
                    f.PinNo, r.REALID, r.USERID, r.REALADD, r.REGDATE,
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
                    CAST(INDATEM AS DATE) AS DATE,
                    DATENAME(WEEKDAY, INDATEM) AS DAY,
                    COUNT(*) AS CNT
                FROM
                    TB_REALINFO
                WHERE
                    INDATEM >= CAST(GETDATE() - 6 AS DATE)
                    AND INDATEM < CAST(GETDATE() + 1 AS DATE)
                GROUP BY
                    CAST(INDATEM AS DATE),
                    DATENAME(WEEKDAY, INDATEM)
                ORDER BY
                    DATE;
            """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
    // 차트데이터(상위검색지역)
    public List<Map<String, Object>> BestSearch() {

        String sql = """
               WITH TopRegions AS (
                   -- 지난 7일간 REGUGUN별 검색 횟수를 계산 (오늘 포함)
                   SELECT
                       r.REGUGUN,
                       r.RESIDO,
                       COUNT(s.REALID) AS TotalSearches
                   FROM
                       TB_REALINFO r
                   LEFT JOIN
                       TB_SEARCHINFO s ON r.REALID = s.REALID
                   WHERE
                       CAST(s.REQDATE AS DATE) >= CAST(GETDATE() - 7 AS DATE) -- 7일 전부터
                       AND CAST(s.REQDATE AS DATE) <= CAST(GETDATE() AS DATE) -- 오늘 포함
                   GROUP BY
                       r.REGUGUN, r.RESIDO
               ),
               TodaySearches AS (
                   -- 오늘 날짜 기준 REGUGUN별 검색 횟수 계산
                   SELECT
                       r.REGUGUN,
                       r.RESIDO,
                       COUNT(s.REALID) AS TodaySearchCount
                   FROM
                       TB_REALINFO r
                   LEFT JOIN
                       TB_SEARCHINFO s ON r.REALID = s.REALID
                   WHERE
                       CAST(s.REQDATE AS DATE) = CAST(GETDATE() AS DATE) -- 오늘 날짜
                   GROUP BY
                       r.REGUGUN, r.RESIDO
               ),
               YesterdaySearches AS (
                   -- 어제 날짜 기준 REGUGUN별 검색 횟수 계산
                   SELECT
                       r.REGUGUN,
                       r.RESIDO,
                       COUNT(s.REALID) AS YesterdaySearchCount
                   FROM
                       TB_REALINFO r
                   LEFT JOIN
                       TB_SEARCHINFO s ON r.REALID = s.REALID
                   WHERE
                       CAST(s.REQDATE AS DATE) = CAST(GETDATE() - 1 AS DATE) -- 어제 날짜
                   GROUP BY
                       r.REGUGUN, r.RESIDO
               )
               -- REGUGUN별 지난 7일간 검색 횟수와 어제, 오늘 검색 횟수를 조회
               SELECT
                   tr.REGUGUN,
                   tr.RESIDO,
                   tr.TotalSearches,
                   ISNULL(ts.TodaySearchCount, 0) AS TodaySearchCount,
                   ISNULL(ys.YesterdaySearchCount, 0) AS YesterdaySearchCount
               FROM
                   TopRegions tr
               LEFT JOIN
                   TodaySearches ts
                   ON tr.REGUGUN = ts.REGUGUN AND tr.RESIDO = ts.RESIDO
               LEFT JOIN
                   YesterdaySearches ys
                   ON tr.REGUGUN = ys.REGUGUN AND tr.RESIDO = ys.RESIDO
               ORDER BY
                   ts.TodaySearchCount DESC; -- 오늘 날짜의 검색 횟수 기준 내림차순 정렬
            """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
    // 카드 데이터(집계 총가입자)
    public List<Map<String, Object>> cardDatas() {

        String sql = """
                SELECT
                    COUNT(CASE
                             WHEN INDATEM >= DATEADD(MONTH, -1, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))
                              AND INDATEM < DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)
                             THEN 1
                          END) AS LAST_MONTH_COUNT,
                    COUNT(CASE
                             WHEN INDATEM >= DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)
                              AND INDATEM < DATEADD(MONTH, 1, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))
                             THEN 1
                          END) AS THIS_MONTH_COUNT
                FROM TB_USERINFO;
            """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
    // 카드 데이터(미답변 문의건수)
    public List<Map<String, Object>> NotAnswerQnAList() {

        String sql = """
                SELECT
                    SUM(CASE
                            WHEN CONVERT(DATE, Q1.INDATEM) = CONVERT(DATE, GETDATE() - 1) THEN 1
                            ELSE 0
                        END) AS YUC,
                    SUM(CASE
                            WHEN CONVERT(DATE, Q1.INDATEM) = CONVERT(DATE, GETDATE()) THEN 1
                            ELSE 0
                        END) AS TUC
                FROM
                    TB_USERQST Q1
                WHERE
                    Q1.FLAG = '0' -- 문의글
                    AND NOT EXISTS (
                        SELECT 1
                        FROM TB_USERQST Q2
                        WHERE Q2.FLAG = '1' -- 답변글
                          AND Q2.CHSEQ = Q1.QSTSEQ
                );
            """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql, null);

        return items;
    }
}
