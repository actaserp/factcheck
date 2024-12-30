package mes.domain.repository.actasRepository;

import mes.domain.entity.actasEntity.TB_REGISTER;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TB_registerRepository extends JpaRepository<TB_REGISTER, Integer> {
    List<TB_REGISTER> findByRegNmContainingIgnoreCase(String regnm);
}
