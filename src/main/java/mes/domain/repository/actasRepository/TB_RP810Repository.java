package mes.domain.repository.actasRepository;

import mes.domain.entity.actasEntity.TB_RP810;
import mes.domain.entity.actasEntity.TB_RP810_PK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface TB_RP810Repository extends JpaRepository<TB_RP810, TB_RP810_PK> {


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM TB_RP810 t WHERE  t.spuncode IN :spuncode")
    void deleteBySpuncode(@Param("spuncode") List<String> spuncode);
}
