package mes.app.tilko.service;

import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TilkoService {

    @Autowired
    SqlRunner sqlRunner;

    // 부동산등기부정보_XML ROWDATA(TB_REALINFOXML)
    public void saveTilkoXML(Map<String, Object> jsonDataMap) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                INSERT INTO TB_REALINFOXML (
                    REALID,
                    PinNo,
                    WksbiAddress,
                    Address,
                    WksbiBalDate,
                    WksbiBalNoTime,
                    IssOffice,
                    IssNo,
                    SumYn,
                    WksbiJrisdictionOffice
                ) VALUES (
                    :REALID,
                    :PinNo,
                    :WksbiAddress,
                    :Address,
                    :WksbiBalDate,
                    :WksbiBalNoTime,
                    :IssOffice,
                    :IssNo,
                    :SumYn,
                    :WksbiJrisdictionOffice
                );
                
                """;
        params.addValue("REALID", jsonDataMap.get("REALID"));
        params.addValue("PinNo", jsonDataMap.get("PinNo"));
        params.addValue("WksbiAddress", jsonDataMap.get("WksbiAddress"));
        params.addValue("Address", jsonDataMap.get("Address"));
        params.addValue("WksbiBalDate", jsonDataMap.get("WksbiBalDate"));
        params.addValue("WksbiBalNoTime", jsonDataMap.get("WksbiBalNoTime"));
        params.addValue("IssOffice", jsonDataMap.get("IssOffice"));
        params.addValue("IssNo", jsonDataMap.get("IssNo"));
        params.addValue("SumYn", jsonDataMap.get("SumYn"));
        params.addValue("WksbiJrisdictionOffice", jsonDataMap.get("WksbiJrisdictionOffice"));
        int realMaxNum = this.sqlRunner.execute(sql,params);

        System.out.println("realMaxNum : " + realMaxNum);

    }
    // registerdataK 저장
    public void saveRegisterDataK(Map<String, Object> jsonDataMap) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
            INSERT INTO TB_REALAOWN (
                REALID,
                RankNo,
                RgsAimCont,
                Receve,
                RgsCaus,
                NomprsAndEtc
            ) VALUES (
                :REALID,
                :RankNo,
                :RgsAimCont,
                :Receve,
                :RgsCaus,
                :NomprsAndEtc
            );
            """;

        // Map 데이터를 SQL 파라미터에 매핑
        params.addValue("REALID", jsonDataMap.get("REALID"));
        params.addValue("RankNo", jsonDataMap.get("RankNo"));
        params.addValue("RgsAimCont", jsonDataMap.get("RgsAimCont"));
        params.addValue("Receve", jsonDataMap.get("Receve"));
        params.addValue("RgsCaus", jsonDataMap.get("RgsCaus"));
        params.addValue("NomprsAndEtc", jsonDataMap.get("NomprsAndEtc"));

        int realMaxNum = this.sqlRunner.execute(sql,params);
    }
    // dataH저장
    public void savedataH(String dataHNum) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                
                """;
        params.addValue("dataHNum", dataHNum);
        int realMaxNum = this.sqlRunner.execute(sql,params);

    }

    // dataHitems저장
    public void savedataHItems(Map<String, Object> dataH) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                INSERT INTO TB_REGISTERDATAHITEMS (
                                REALID,
                                SeqNo,
                                EstateRightDisplay,
                                OwnJuris,
                                RankNo,
                                CrtResn,
                                DstInfo
                            ) VALUES (
                                :REALID,
                                :SeqNo,
                                :EstateRightDisplay,
                                :OwnJuris,
                                :RankNo,
                                :CrtResn,
                                :DstInfo
                            );
                """;
        // Map 데이터를 SQL 파라미터에 매핑
        params.addValue("REALID", dataH.get("REALID"));
        params.addValue("SeqNo", dataH.get("SeqNo"));
        params.addValue("EstateRightDisplay", dataH.get("EstateRightDisplay"));
        params.addValue("OwnJuris", dataH.get("OwnJuris"));
        params.addValue("RankNo", dataH.get("RankNo"));
        params.addValue("CrtResn", dataH.get("CrtResn"));
        params.addValue("DstInfo", dataH.get("DstInfo"));

        int realMaxNum = this.sqlRunner.execute(sql,params);

    }

    // dataE저장
    public void savedataE(Map<String, Object> dataMap) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
            INSERT INTO TB_REALBOWN (
                REALID,
                RankNo,
                RgsAimCont,
                Receve,
                RgsCaus,
                NomprsAndEtc
            ) VALUES (
                :REALID,
                :RankNo,
                :RgsAimCont,
                :Receve,
                :RgsCaus,
                :NomprsAndEtc
            );
            """;

        // Map 데이터를 SQL 파라미터에 매핑
        params.addValue("REALID", dataMap.get("REALID"));
        params.addValue("RankNo", dataMap.get("RankNo"));
        params.addValue("RgsAimCont", dataMap.get("RgsAimCont"));
        params.addValue("Receve", dataMap.get("Receve"));
        params.addValue("RgsCaus", dataMap.get("RgsCaus"));
        params.addValue("NomprsAndEtc", dataMap.get("NomprsAndEtc"));

        int realMaxNum = this.sqlRunner.execute(sql,params);

    }

    // dataJ저장(매매정보)
    public void savedataJ(Map<String, Object> dataHNum) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                
                """;
        params.addValue("dataJNum", dataHNum.get("data"));
        int realMaxNum = this.sqlRunner.execute(sql,params);

    }

    // dataJ 하위 TradeAmount저장
    public void saveTradeAmount(Map<String, Object> TradeAmount) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                INSERT INTO TB_TradeAmount (REALID, Amount, UpdateResn)
                        VALUES (:REALID, :Amount,"")
                """;
        params.addValue("REALID", TradeAmount.get("REALID"));
        params.addValue("Amount", TradeAmount.get("Amount"));
        int realMaxNum = this.sqlRunner.execute(sql,params);

    }

    // dataJ 하위 Items 저장
    public void saveItemsdata(Map<String, Object> dataHNum) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                
                """;
        params.addValue("dataJNum", dataHNum.get("data"));
        int realMaxNum = this.sqlRunner.execute(sql,params);

    }

    // dataJ 하위 없는 경우
    public void savedataJItems(Map<String, Object> dataHNum) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                
                """;
        params.addValue("dataJNum", dataHNum.get("data"));
        int realMaxNum = this.sqlRunner.execute(sql,params);

    }
    // Summary저장
    public void saveSummary(Map<String, Object> summaryData) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
            INSERT INTO TB_REALSUMMARY (
                REALID,
                UniqueNo,
                Gubun,
                Address,
                PrintDate
            ) VALUES (
                :REALID,
                :UniqueNo,
                :Gubun,
                :Address,
                :PrintDate
            );
            """;

        // Map 데이터를 SQL 파라미터에 매핑
        params.addValue("REALID", summaryData.get("REALID"));
        params.addValue("UniqueNo", summaryData.get("UniqueNo"));
        params.addValue("Gubun", summaryData.get("Gubun"));
        params.addValue("Address", summaryData.get("Address"));
        params.addValue("PrintDate", summaryData.get("PrintDate"));
        int realMaxNum = this.sqlRunner.execute(sql,params);

    }

    // REALINFO 테이블 최대 아이디값 + 1 조회
    public Map<String, Object> getMaxOfRealinfo() {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                SELECT COALESCE(MAX(REALID), 0) + 1 AS NextID
                FROM TB_REALINFO;
                """;

        Map<String, Object> realMaxNum = this.sqlRunner.getRow(sql,params);

        return realMaxNum;
    }

    // 부동산등기부정보(TB_REALINFO)
    public void saveTilko(Map<String, Object> jsonDataMap) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                INSERT INTO TB_REALINFO (
                    USERID,
                    REQDATE,
                    REALADD,
                    REGDATE,
                    RESIDO,
                    REGUGUN,
                    REMOK,
                    REWON,
                    REWONDATE,
                    REJIMOK,
                    REAREA,
                    REAMOUNT,
                    RESEQ,
                    REMAXAMT,
                    INDATEM,
                    REALSCORE,
                    REALPOINT,
                    RELASTDATE,
                    REALGUBUN
                ) VALUES (
                    :USERID,      -- USERID
                    :REQDATE,     -- REQDATE (YYYYMMDD 형식)
                    :REALADD, -- REALADD
                    '20250121',     -- REGDATE
                    '1234567890',   -- RESIDO
                    '금천구',       -- REGUGUN
                    '건물',         -- REMOK
                    '홍길동',       -- REWON
                    '20250120',     -- REWONDATE
                    '상업지구',      -- REJIMOK
                    123.456,        -- REAREA (decimal 형식)
                    1000000,        -- REAMOUNT
                    1,              -- RESEQ
                    2000000,        -- REMAXAMT
                    GETDATE(),      -- INDATEM (현재 날짜와 시간)
                    85,             -- REALSCORE
                    90,             -- REALPOINT
                    '20250122',     -- RELASTDATE
                    '건물정보'        -- REALGUBUN
                );
                """;
        params.addValue("USERID", jsonDataMap.get("USERID"));
        params.addValue("REQDATE", jsonDataMap.get("REQDATE"));
        params.addValue("REALADD", jsonDataMap.get("REALADD"));

        int realMaxNum = this.sqlRunner.execute(sql,params);

    }

    // summarydataA 저장
    public void saveSummaryDataA(Map<String, Object> jsonDataMap) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
            INSERT INTO TB_SummaryDataA (
                REALID,
                RegisteredHolder,
                RegistrationNumber,
                FinalShare,
                Address,
                RankNo
            ) VALUES (
                :REALID,
                :RegisteredHolder,
                :RegistrationNumber,
                :FinalShare,
                :Address,
                :RankNo
            );
            """;

        // Map 데이터를 SQL 파라미터에 매핑
        params.addValue("REALID", jsonDataMap.get("REALID"));
        params.addValue("RegisteredHolder", jsonDataMap.get("RegisteredHolder"));
        params.addValue("RegistrationNumber", jsonDataMap.get("RegistrationNumber"));
        params.addValue("FinalShare", jsonDataMap.get("FinalShare"));
        params.addValue("Address", jsonDataMap.get("Address"));
        params.addValue("RankNo", jsonDataMap.get("RankNo"));

        int realMaxNum = this.sqlRunner.execute(sql,params);
    }

    // summarydataK 저장
    public void saveSummaryDataK(Map<String, Object> jsonDataMap) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
            INSERT INTO TB_SUMMARYDATAK (
                REALID,
                RankNo,
                Purpose,
                ReceiptInfo,
                Information,
                TargetOwner
            ) VALUES (
                :REALID,
                :RankNo,
                :Purpose,
                :ReceiptInfo,
                :Information,
                :TargetOwner
            );
            """;

        // Map 데이터를 SQL 파라미터에 매핑
        params.addValue("REALID", jsonDataMap.get("REALID"));
        params.addValue("RankNo", jsonDataMap.get("RankNo"));
        params.addValue("Purpose", jsonDataMap.get("Purpose"));
        params.addValue("ReceiptInfo", jsonDataMap.get("ReceiptInfo"));
        params.addValue("Information", jsonDataMap.get("Information"));
        params.addValue("TargetOwner", jsonDataMap.get("TargetOwner"));

        int realMaxNum = this.sqlRunner.execute(sql,params);
    }

    // summarydataE 저장
    public void saveSummaryDataE(Map<String, Object> jsonDataMap) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
        INSERT INTO TB_SUMMARYDATAE (
            REALID,
            RankNo,
            Purpose,
            ReceiptInfo,
            Information,
            TargetOwner
        ) VALUES (
            :REALID,
            :RankNo,
            :Purpose,
            :ReceiptInfo,
            :Information,
            :TargetOwner
        );
        """;

        // Map 데이터를 SQL 파라미터에 매핑
        params.addValue("REALID", jsonDataMap.get("REALID"));
        params.addValue("RankNo", jsonDataMap.get("RankNo"));
        params.addValue("Purpose", jsonDataMap.get("Purpose"));
        params.addValue("ReceiptInfo", jsonDataMap.get("ReceiptInfo"));
        params.addValue("Information", jsonDataMap.get("Information"));
        params.addValue("TargetOwner", jsonDataMap.get("TargetOwner"));

        // SQL 실행
        int rowsAffected = this.sqlRunner.execute(sql, params);
    }
    // Rights 저장
    public void saveRights(Map<String, Object> jsonDataMap) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
        INSERT INTO TB_RIGHTS (
            REALID,
            RankNo,
            Gubun,
            TargetOwner,
            Information,
            OriginalText
        ) VALUES (
            :REALID,
            :RankNo,
            :Gubun,
            :TargetOwner,
            :Information,
            :OriginalText
        );
    """;

        // Map 데이터를 SQL 파라미터에 매핑
        params.addValue("REALID", jsonDataMap.get("REALID"));
        params.addValue("RankNo", jsonDataMap.get("RankNo"));
        params.addValue("Gubun", jsonDataMap.get("Gubun"));
        params.addValue("TargetOwner", jsonDataMap.get("TargetOwner"));
        params.addValue("Information", jsonDataMap.get("Information"));
        params.addValue("OriginalText", jsonDataMap.get("OriginalText"));

        // SQL 실행
        int rowsAffected = this.sqlRunner.execute(sql, params);
    }

    // Seniority 저장
    public void saveSeniority(Map<String, Object> jsonDataMap) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
        INSERT INTO TB_SENIORITY (
            REALID,
            RankNo,
            Gubun,
            TargetOwner,
            Amount,
            CurCode,
            Warning,
            OriginalText
        ) VALUES (
            :REALID,
            :RankNo,
            :Gubun,
            :TargetOwner,
            :Amount,
            :CurCode,
            :Warning,
            :OriginalText
        );
    """;

        // Map 데이터를 SQL 파라미터에 매핑
        params.addValue("REALID", jsonDataMap.get("REALID"));
        params.addValue("RankNo", jsonDataMap.get("RankNo"));
        params.addValue("Gubun", jsonDataMap.get("Gubun"));
        params.addValue("TargetOwner", jsonDataMap.get("TargetOwner"));
        params.addValue("Amount", jsonDataMap.get("Amount"));
        params.addValue("CurCode", jsonDataMap.get("CurCode"));
        params.addValue("Warning", jsonDataMap.get("Warning"));
        params.addValue("OriginalText", jsonDataMap.get("OriginalText"));

        // SQL 실행
        int rowsAffected = this.sqlRunner.execute(sql, params);
    }
    // 분류관리 조회
    public List<Map<String, Object>> getComcode() {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
        SELECT *
        FROM TB_REGISTER;
    """;

        // SQL 실행
        List<Map<String, Object>> resultRows = this.sqlRunner.getRows(sql, params);
        return resultRows;
    }
    // 공통코드 386(최저점수) 조회
    public Map<String, Object> getLessScore() {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
        SELECT Value
        FROM user_code
        WHERE Parent_id = 386;
    """;

        // SQL 실행
        Map<String, Object> resultRow = this.sqlRunner.getRow(sql, params);
        return resultRow;
    }


    // api 사용 로그 쌓기
    public void saveTilkoApiLog() {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                
                """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
    }

}
