package mes.app.customer_management.service;

import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CM_QnaService {
    @Autowired
    SqlRunner sqlRunner;

    // FAQ 답변등록
    public void saveRegisterScore() {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                
                """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
    }
}
