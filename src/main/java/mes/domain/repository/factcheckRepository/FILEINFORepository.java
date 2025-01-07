package mes.domain.repository.factcheckRepository;

import mes.domain.entity.factcheckEntity.TB_BBSINFO;
import mes.domain.entity.factcheckEntity.TB_FILEINFO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FILEINFORepository extends JpaRepository<TB_FILEINFO, Integer> {

    @Query("SELECT f FROM TB_FILEINFO f WHERE f.CHECKSEQ = :checkseq AND f.bbsseq = :bbsseq")
    List<TB_FILEINFO> findAllByCheckseqAndBbsseq(@Param("checkseq") String checkseq, @Param("bbsseq") int bbsseq);
}
