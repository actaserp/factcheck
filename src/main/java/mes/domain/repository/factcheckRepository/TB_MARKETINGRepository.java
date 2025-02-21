package mes.domain.repository.factcheckRepository;

import mes.domain.entity.factcheckEntity.TB_MARKETING;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  TB_MARKETINGRepository extends JpaRepository<TB_MARKETING, Integer> {

}
