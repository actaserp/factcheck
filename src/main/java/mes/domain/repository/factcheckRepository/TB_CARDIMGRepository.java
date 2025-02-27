package mes.domain.repository.factcheckRepository;

import mes.domain.entity.factcheckEntity.TB_CARDIMG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TB_CARDIMGRepository  extends JpaRepository<TB_CARDIMG, Integer> {
}
