package mes.app.IssueInquiry.service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.entity.User;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class IssueInquiryService {

  @Autowired
  SqlRunner sqlRunner;

  public List<Map<String, Object>> getList(String startDate, String endDate, String searchKeywords, String userID, int realId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    StringBuilder sql = new StringBuilder("""
         select
          *
          from TB_REALINFO
          where 1=1
        """);

    // startDate 필터링
    if (startDate != null && !startDate.isEmpty()) {
      sql.append(" AND RELASTDATE >= :startDate ");
      params.addValue("startDate", startDate);
    }

    // endDate 필터링
    if (endDate != null && !endDate.isEmpty()) {
      sql.append(" AND RELASTDATE <= :endDate ");
      params.addValue("endDate", endDate);
    }

    // 주소 검색어 필터링 (부분 검색 가능)
    if (searchKeywords != null && !searchKeywords.isEmpty()) {
      sql.append(" AND REALADD LIKE :searchKeywords ");
      params.addValue("searchKeywords", "%" + searchKeywords + "%");
    }
    // realId 필터링
    if (realId > 0) {
      sql.append(" AND realId = :realId ");
      params.addValue("realId", realId);
    }

    // 특정 사용자 ID 필터링
    if (userID != null && !userID.isEmpty()) {
      sql.append(" AND USERID = :userID ");
      params.addValue("userID", userID);
    }

    // 최신 날짜순으로 정렬
    sql.append("  ORDER BY INDATEM DESC ");

    // 로그 출력 (디버깅 용도)
//    log.info("등기부 발급 List SQL: {}", sql);
//    log.info("SQL Parameters: {}", params.getValues());
    return sqlRunner.getRows(sql.toString(), params);
  }

  public List<Map<String, Object>> getDetails(String REALID) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    StringBuilder sql = new StringBuilder("""
         SELECT
           ri.REALID,
           ri.USERID,
           ri.REQDATE,
           ri.REALADD,
           ri.REGDATE,
           ri.RESIDO,
           ri.REGUGUN,
           ri.REMOK,
           ri.REWON,
           ri.REWONDATE,
           ri.REJIMOK,
           ri.REAREA,
           ri.REAMOUNT,
           ri.RESEQ,
           ri.REMAXAMT,
           ri.INDATEM,
           ri.REALSCORE,
           ri.REALPOINT,
           ri.RELASTDATE,
           ri.REALGUBUN,
           ri.PDFFILENAME,
          COALESCE(ra.RankNo, '-') AS RankNo_A,
          COALESCE(ra.RgsAimCont, '-') AS RgsAimCont_A,
          COALESCE(ra.Receve, '-') AS Receve_A,
          COALESCE(ra.RgsCaus, '-') AS RgsCaus_A,
          COALESCE(ra.NomprsAndEtc, '-') AS NomprsAndEtc_A,
          COALESCE(rb.RankNo, '-') AS RankNo_B,
          COALESCE(rb.RgsAimCont, '-') AS RgsAimCont_B,
          COALESCE(rb.Receve, '-') AS Receve_B,
          COALESCE(rb.RgsCaus, '-') AS RgsCaus_B,
          COALESCE(rb.NomprsAndEtc, '-') AS NomprsAndEtc_B
       FROM MOB_FACTCHK.dbo.TB_REALINFO ri
       LEFT JOIN MOB_FACTCHK.dbo.TB_REALAOWN ra ON ri.REALID = ra.REALID
       LEFT JOIN MOB_FACTCHK.dbo.TB_REALBOWN rb ON ri.REALID = rb.REALID
       WHERE 1=1
        """);
    if (REALID != null && !REALID.isEmpty()) {
      sql.append(" AND ri.REALID = :REALID ");
      params.addValue("REALID", REALID);
    }
    sql.append("""
        ORDER BY
         RELASTDATE DESC
        """);
//    log.info("발급 상세 내역 List SQL: {}", sql);
//    log.info("SQL Parameters: {}", params.getValues());
    return sqlRunner.getRows(sql.toString(), params);
  }

  //등기부 api 조회
  public List<Map<String, Object>> getAPIList(String startDate, String endDate, String searchKeywords) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    StringBuilder sql = new StringBuilder("""
        select
        *
        from TB_REALINFO
        where 1=1
        """);
    if (startDate != null && !startDate.isEmpty()) {
      sql.append(" AND RELASTDATE >= :startDate ");
      params.addValue("startDate", startDate);
    }
    if (endDate != null && !endDate.isEmpty()) {
      sql.append(" AND RELASTDATE <= :endDate ");
      params.addValue("endDate", endDate);
    }
    if (searchKeywords != null && !searchKeywords.isEmpty()) {
      sql.append(" AND REALADD LIKE :searchKeywords ");
      params.addValue("searchKeywords", "%" + searchKeywords + "%");
    }
    sql.append("""
        ORDER BY
         RELASTDATE DESC
        """);
//    log.info("등기부API 발급 List SQL: {}", sql);
//    log.info("SQL Parameters: {}", params.getValues());
    return sqlRunner.getRows(sql.toString(), params);
  }

  public List<Map<String, Object>> SaveViewHistory(int REALID, User user) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("REALID", REALID);
    params.addValue("userid", user.getUsername());

    String sql = """
        INSERT INTO TB_SEARCHINFO (USERID, REQDATE, REALID) 
        VALUES (:userid, GETDATE(), :REALID);
        """;

//    log.info("등기부API 조회 저장 SQL: {}", sql);
//    log.info("SQL Parameters: {}", params.getValues());

    sqlRunner.execute(sql, params);  // INSERT 실행 (결과 반환 필요 없음)

  //  log.info("조회 기록 저장 완료 - realId: {}, user: {}", REALID, user.getUsername());
    return null;
  }

  public Optional<String> findPdfFilenameByRealId(int realId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("realId", realId);

    String sql = "SELECT PDFFILENAME FROM TB_REALINFO WHERE REALID = :realId";

    try {
      // SQL 실행 후 결과 조회
//      log.info("등기부API 발급 List SQL: {}", sql);
//      log.info("SQL Parameters: {}", params.getValues());
      List<Map<String, Object>> result = sqlRunner.getRows(sql, params);

      if (!result.isEmpty() && result.get(0).get("PDFFILENAME") != null) {
        return Optional.of((String) result.get(0).get("PDFFILENAME"));
      }
    } catch (Exception e) {
      log.info("PDF 파일명을 조회하는 중 오류 발생: {}", e.getMessage(), e);
    }

    return Optional.empty(); // 결과가 없으면 빈 Optional 반환
  }

  //고유번호 찾기
  public List<Map<String, Object>> getFindPinNo(int realid) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("realid", realid);
    String sql = """
     SELECT tr.REALID, tr.REALADD, tr.REGDATE, tr.INDATEM, tr.REALSCORE, tr2.PinNo
           FROM TB_REALINFO tr
           LEFT JOIN TB_REALINFOXML tr2 ON tr2.REALID = tr.REALID
           WHERE tr2.PinNo = (
               SELECT tr2_sub.PinNo
               FROM TB_REALINFOXML tr2_sub
               WHERE tr2_sub.REALID = :realid
           )
           ORDER BY tr.INDATEM DESC;
    """;
    log.info("고유번호SQL: {}", sql);
    log.info("SQL Parameters, 고유번호 : {}", params.getValues());
    return sqlRunner.getRows(sql, params);
  }

  //갑구
  public List<Map<String, Object>> getFindnK(int realid) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("realid", realid);

    String sql = """
          SELECT k.*,tr.INDATEM, tr.REALSCORE FROM TB_SUMMARYDATAK k 
              LEFT JOIN TB_REALINFO tr ON k.REALID = tr.REALID
              WHERE k.REALID = :realid
               ORDER BY
                   TRY_CAST(REPLACE(RankNo, '-', '') +
                                            CASE WHEN RankNo LIKE '%-%' THEN '' ELSE '0' END AS INT) ASC,
                                   CASE WHEN ISNUMERIC(RankNo) = 1 THEN 0 ELSE 1 END,
                                   RankNo ASC;
    """;
    log.info("이력 갑구 SQL: {}", sql);
    log.info("SQL Parameters, 이력 갑구 : {}", params.getValues());
    return sqlRunner.getRows(sql, params);

  }

  //을구
  public List<Map<String, Object>> getFindE(int realid) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("realid", realid);

    String sql = """
      SELECT e.*, tr.INDATEM, tr.REALSCORE FROM TB_SUMMARYDATAE e
        LEFT JOIN TB_REALINFO tr ON e.REALID = tr.REALID
        WHERE e.REALID = :realid
          ORDER BY
             TRY_CAST(REPLACE(RankNo, '-', '') +
                                      CASE WHEN RankNo LIKE '%-%' THEN '' ELSE '0' END AS INT) ASC,
                             CASE WHEN ISNUMERIC(RankNo) = 1 THEN 0 ELSE 1 END,
                             RankNo ASC;
    """;
    log.info("이력 을구 SQL: {}", sql);
    log.info("SQL Parameters, 이력 을구 : {}", params.getValues());
    return sqlRunner.getRows(sql, params);

  }
}
