package mes.app.user_management.service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.repository.actasRepository.TB_USERINFORepository;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClientRegistryService {

    @Autowired
    TB_USERINFORepository tb_userinfoRepository;

    @Autowired
    SqlRunner sqlRunner;

    public List<Map<String, Object>> getList(String startDate, String endDate, String searchUserNm) {
      MapSqlParameterSource params = new MapSqlParameterSource();
      StringBuilder sql = new StringBuilder("""
        SELECT
           tu.USERID,
           tu.USERGU AS userGU,
           tu.USERSIDO AS usersido,
           tu.USERNM AS userNm,
           tu.SEXYN AS sexYN,
           tu.USERHP AS userHP,
           tu.POSTCD,
           tu.USERADDR AS userAddr,
           tu.USERMAIL AS userEmail,
           tu.INDATEM AS indatem, -- 등록일자
           COALESCE(COUNT(ri.USERID), 0) AS InquiryCount
        FROM
            TB_USERINFO tu
        LEFT JOIN TB_REALINFO ri
        ON tu.USERID = ri.USERID
            AND ri.RELASTDATE >= :startDate
            AND ri.RELASTDATE <= :endDate
        WHERE 1=1
       """);

      // WHERE 절 동적 조건 추가
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND tu.INDATEM >= :startDate "); // 공백 추가
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
          LocalDate parsedEndDate = LocalDate.parse(endDate);
          LocalDate nextDay = parsedEndDate.plusDays(1);

            sql.append(" AND tu.INDATEM <= :endDate "); // 공백 추가
            params.addValue("endDate", nextDay.toString());
        }
        if (searchUserNm != null && !searchUserNm.isEmpty()) {
            sql.append(" AND tu.USERNM LIKE :searchUserNm "); // 공백 추가
            params.addValue("searchUserNm", "%" + searchUserNm + "%");
        }


        // GROUP BY 및 ORDER BY 추가
        sql.append("""
        GROUP BY
         tu.USERID,
         tu.USERGU,
         tu.USERSIDO,
         tu.USERNM,
         tu.SEXYN,
         tu.USERHP,
         tu.POSTCD,
         tu.USERADDR,
         tu.USERMAIL,
         tu.INDATEM
        """);

            sql.append("""
        ORDER BY
           tu.INDATEM DESC
        """);

//      log.info("회원관리List SQL: {}", sql);
//      log.info("SQL Parameters: {}", params.getValues());

      return sqlRunner.getRows(sql.toString(), params);
  }

    public List<Map<String, Object>> getModalReadList(String startDate, String endDate, String searchUserNm) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
            SELECT
                 re.USERID,
                 re.REQDATE,
                 re.REALADD,
                 tu.USERNM
            FROM TB_REALINFO re
            JOIN TB_USERINFO tu\s
                ON tu.USERID = re.USERID
            WHERE
        """);

        // 날짜 조건 처리
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            sql.append(" re.REQDATE BETWEEN :startDate AND :endDate  ");
            params.addValue("startDate", startDate);
            params.addValue("endDate", endDate);
        } else {
            if (startDate != null && !startDate.isEmpty()) {
                sql.append(" AND re.REQDATE >= :startDate ");
                params.addValue("startDate", startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql.append(" AND re.REQDATE <= :endDate ");
                params.addValue("endDate", endDate);
            }
        }

        // USERID 조건 처리
        if (searchUserNm != null && !searchUserNm.isEmpty()) {
            sql.append(" AND re.USERID = :searchUserNm ");
            params.addValue("searchUserNm", searchUserNm);
        }

        sql.append("""
        ORDER BY re.REQDATE ASC, re.REALADD ASC
        """);

        // 디버깅용 로그 출력
//        log.info("모달 조회 리스트 SQL: {}", sql);
//        log.info("SQL Parameters: {}", params.getValues());

        // SQL 실행
        return sqlRunner.getRows(sql.toString(), params);
    }
    // 탈퇴 계정 조회
    public List<Map<String, Object>> unactiveUser(String startDate, String endDate, String searchUserNm) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
        SELECT
            tu.USERID,
            tu.USERGU AS userGU,
            tu.USERSIDO AS usersido,
            tu.USERNM AS userNm,
            tu.SEXYN AS sexYN,
            tu.USERHP AS userHP,
            tu.POSTCD,
            tu.USERADDR AS userAddr,
            tu.USERMAIL AS userEmail,
            tu.INDATEM AS indatem, -- 등록일자
            tu.WDRAWDATE
         FROM
             TB_USERINFO tu
         WHERE 1=1
             AND tu.USEYN = 0
       """);

        // WHERE 절 동적 조건 추가
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND tu.INDATEM >= :startDate "); // 공백 추가
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDate parsedEndDate = LocalDate.parse(endDate);
            LocalDate nextDay = parsedEndDate.plusDays(1);

            sql.append(" AND tu.INDATEM <= :endDate "); // 공백 추가
            params.addValue("endDate", nextDay.toString());
        }
        if (searchUserNm != null && !searchUserNm.isEmpty()) {
            sql.append(" AND tu.USERNM LIKE :searchUserNm "); // 공백 추가
            params.addValue("searchUserNm", "%" + searchUserNm + "%");
        }


        // GROUP BY 및 ORDER BY 추가
        sql.append("""
        GROUP BY
         tu.USERID,
         tu.USERGU,
         tu.USERSIDO,
         tu.USERNM,
         tu.SEXYN,
         tu.USERHP,
         tu.POSTCD,
         tu.USERADDR,
         tu.USERMAIL,
         tu.INDATEM,
         tu.WDRAWDATE
        """);

        sql.append("""
        ORDER BY
           tu.INDATEM DESC
        """);

//      log.info("회원관리List SQL: {}", sql);
//      log.info("SQL Parameters: {}", params.getValues());

        return sqlRunner.getRows(sql.toString(), params);
    }

}
