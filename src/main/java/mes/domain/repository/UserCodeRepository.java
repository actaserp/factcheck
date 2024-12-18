package mes.domain.repository;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import mes.domain.entity.UserCode;
import org.springframework.data.jpa.repository.Query;

public interface UserCodeRepository extends JpaRepository<UserCode, Integer>{

	List<UserCode> findByCodeAndParentIdIsNull(String parentCode);

	List<UserCode> findByCodeAndParentId(String code, Integer parentId);

	UserCode getUserCodeById(Integer id);

	List<UserCode> findByCode (String code);



	List<UserCode> findByParentId(Integer parentId);

	List<UserCode> findByCodeAndValue (String code, String value);


	@Query("SELECT COUNT(c) > 0 FROM UserCode c WHERE LOWER(c.code) = LOWER(:code)")
	boolean existsByCode(@Param("code") String code);

	@Query("SELECT COUNT(c) FROM UserCode c WHERE LOWER(c.code) = LOWER(:code)")
	int countByCode(@Param("code") String code);
}
