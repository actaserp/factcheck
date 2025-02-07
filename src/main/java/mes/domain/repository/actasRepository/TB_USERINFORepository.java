package mes.domain.repository.actasRepository;

import mes.domain.entity.actasEntity.TB_USERINFO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TB_USERINFORepository extends JpaRepository<TB_USERINFO, String> {

    Optional<TB_USERINFO> findByUserIdAndUseYn(String userId, String useYn);
}
