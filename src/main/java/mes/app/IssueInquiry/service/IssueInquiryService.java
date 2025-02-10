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

  public List<Map<String, Object>> getList(String startDate, String endDate, String searchKeywords, String userID) {
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
    if (userID != null && !userID.isEmpty()) {
      sql.append("  and USERID = :userID ");
      params.addValue("userID",userID );
    }
    sql.append("""
        ORDER BY
         RELASTDATE DESC
        """);
    // log.info("등기부 발급 List SQL: {}", sql);
      //log.info("SQL Parameters: {}", params.getValues());
    return sqlRunner.getRows(sql.toString(), params);
  }

  public List<Map<String, Object>> getDetails(String REALID) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    StringBuilder sql = new StringBuilder("""
        select
       *
        from TB_REALINFO
        where 1=1
        """);
    if (REALID != null && !REALID.isEmpty()) {
      sql.append(" AND REALID LIKE :REALID ");
      params.addValue("REALID", "%" + REALID + "%");
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

    sqlRunner.execute(sql, params);  // 🚀 INSERT 실행 (결과 반환 필요 없음)

    log.info("조회 기록 저장 완료 - realId: {}, user: {}", REALID, user.getUsername());
    return null;
  }

  public Optional<String> findPdfFilenameByRealId(int realId) {
    if (realId <= 0) {
      return Optional.empty(); // 유효하지 않은 realId
    }

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("realId", realId);

    String sql = "SELECT PDFFILENAME FROM TB_REALINFO WHERE REALID = :realId";

    try {
      // SQL 실행 후 결과 조회
      List<Map<String, Object>> result = sqlRunner.getRows(sql, params);

      if (!result.isEmpty() && result.get(0).get("PDFFILENAME") != null) {
        return Optional.of((String) result.get(0).get("PDFFILENAME"));
      }
    } catch (Exception e) {
      // 예외 발생 시 로그 출력 (개발 및 운영 시 오류 확인)
      System.err.println("PDF 파일명을 조회하는 중 오류 발생: " + e.getMessage());
    }
    return Optional.empty(); // 결과가 없으면 빈 Optional 반환
  }


}
