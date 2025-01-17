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

    // api 사용 로그 쌓기
    public void saveTilkoApiLog() {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = """
                
                """;

        List<Map<String,Object>> items = this.sqlRunner.getRows(sql,params);
    }

}
