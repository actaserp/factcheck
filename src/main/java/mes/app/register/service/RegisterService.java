package mes.app.register.service;

import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RegisterService {

    @Autowired
    SqlRunner sqlRunner;

    // 상세보기
    public List<Map<String, Object>> cardDetail(int REALID) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                
                """;

        params.addValue("REALID", REALID);
        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
        return items;
    }

    // 점수이력
    public List<Map<String, Object>> deductionDetail(int REALID) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                SELECT * FROM TB_REALHIS WHERE REALID = :REALID
                """;

        params.addValue("REALID", REALID);
        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
        return items;
    }
}
