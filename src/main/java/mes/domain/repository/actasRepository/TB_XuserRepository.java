package mes.domain.repository.actasRepository;

import mes.domain.entity.actasEntity.TB_XUSERS;
import mes.domain.entity.actasEntity.TB_XUSERSId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
@Repository
public interface TB_XuserRepository extends JpaRepository<TB_XUSERS, TB_XUSERSId> {

    void deleteById_userid(String userid);

    @Transactional
    @Modifying
    @Query(value = "UPDATE TB_XUSERS SET shapass = :pw, passwd1 = :password, passwd2 = :password WHERE userid = :userid", nativeQuery = true)
    void PasswordChange(@Param("pw") String pw, @Param("userid") String userid, @Param("password") String password);

}