package mes.app.GradeManager.service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.entity.actasEntity.TB_GRADEINFO;
import mes.domain.repository.actasRepository.TB_GradeinfoRepository;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TB_GRADEINFOService {

    @Autowired
    TB_GradeinfoRepository tb_gradeinfoRepository;

    @Autowired
    SqlRunner sqlRunner;

    public void saveGrade(Map<String, Object> formData) {

        TB_GRADEINFO entity = new TB_GRADEINFO();

        entity.setGrId((String) formData.get("grid"));
        entity.setGrScore01(formData.get("grScore01") != null ? Integer.valueOf(formData.get("grScore01").toString()) : null);
        entity.setGrScore02(formData.get("grScore02") != null ? Integer.valueOf(formData.get("grScore02").toString()) : null);
        entity.setGrColor((String) formData.get("grcolor"));
        entity.setGrFace((String)formData.get("grface"));
        entity.setGrFlag((String) formData.get("grflag"));
        entity.setGrMood((String) formData.get("grmood"));
        entity.setGrIcon((String) formData.get("gricon"));

        // 데이터 저장
        tb_gradeinfoRepository.save(entity);
    }

    public List<Map<String, Object>> getList(String searchInput) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
        SELECT
            tg.grid,
            uc1.Value AS grid_display,
            tg.grScore01,
            tg.grScore02,
            tg.grface,
            uc2.Value AS grface_display,
            tg.grcolor,
            tg.grflag,
            uc3.Value AS grflag_display,
            tg.grmood,
            uc4.Value AS grmood_display,
            tg.gricon,
            uc5.Value AS gricon_display
        FROM TB_GRADEINFO tg
        LEFT JOIN user_code uc1 ON uc1.Code = tg.grid
        LEFT JOIN user_code uc2 ON uc2.Code = tg.grface
        LEFT JOIN user_code uc3 ON uc3.Code = tg.grflag
        LEFT JOIN user_code uc4 ON uc4.Code = tg.grmood
        LEFT JOIN user_code uc5 ON uc5.Code = tg.gricon
        WHERE 1=1
        """);
        if (searchInput != null && !searchInput.isEmpty()) {
            sql.append(" AND uc3.Value LIKE :grflag_display");
            params.addValue("grflag_display", "%" + searchInput + "%");
        }
        log.info("Generated SQL: {}", sql);
        log.info("SQL Parameters: {}", params.getValues());
        return sqlRunner.getRows(sql.toString(), params);
    }

    public void deleteRegisterById(String grid) {
        if (grid == null || grid.isEmpty()) {
            throw new IllegalArgumentException("삭제할 ID가 유효하지 않습니다.");
        }

        if (!tb_gradeinfoRepository.existsById(grid)) {
            throw new EntityNotFoundException("ID에 해당하는 데이터가 존재하지 않습니다.");
        }

        tb_gradeinfoRepository.deleteById(grid);
    }

}
