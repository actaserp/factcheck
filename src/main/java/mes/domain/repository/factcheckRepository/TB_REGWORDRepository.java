package mes.domain.repository.factcheckRepository;

import mes.domain.entity.factcheckEntity.TB_REGWORD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TB_REGWORDRepository extends JpaRepository<TB_REGWORD, Integer> {

  @Transactional
  @Modifying
  @Query("DELETE FROM TB_REGWORD r WHERE r.regSeq = :regSeq")
  void deleteByRegSeq(int regSeq);

}
