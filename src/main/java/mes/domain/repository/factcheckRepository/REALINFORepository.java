package mes.domain.repository.factcheckRepository;

import mes.domain.entity.factcheckEntity.TB_REALINFO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface REALINFORepository extends JpaRepository<TB_REALINFO, Integer> {
}
