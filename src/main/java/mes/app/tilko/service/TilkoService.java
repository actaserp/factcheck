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
                INSERT INTO TB_REGISTERDATAE (
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
        params.addValue("REALID", dataMap.get("REALID"));
        params.addValue("RankNo", dataMap.get("RankNo"));
        params.addValue("RgsAimCont", dataMap.get("RgsAimCont"));
        params.addValue("Receve", dataMap.get("Receve"));
        params.addValue("RgsCaus", dataMap.get("RgsCaus"));
        params.addValue("NomprsAndEtc", dataMap.get("NomprsAndEtc"));
        int realMaxNum = this.sqlRunner.execute(sql,params);

    }

    // dataJ저장
    public void savedataJ(Map<String, Object> dataHNum) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                
                """;
        params.addValue("dataJNum", dataHNum.get("data"));
        int realMaxNum = this.sqlRunner.execute(sql,params);

    }

    // dataJ 하위 TradeAmount저장
    public void saveTradeAmount(Map<String, Object> dataHNum) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                
                """;
        params.addValue("dataJNum", dataHNum.get("data"));
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
                INSERT INTO MOB_FACTCHK.dbo.TB_REALINFO (
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
                    :REQDATE, -- REALADD
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
                
                """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
    }



    // api 사용 로그 쌓기
    public void saveTilkoApiLog() {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                
                """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
    }

}
