package superapp.dal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import superapp.data.UserEntity;
import superapp.data.UserPK;

import java.util.List;

@Repository
public interface UserEntityRepository extends PagingAndSortingRepository<UserEntity, UserPK> {
    @Query(value = "SELECT * FROM USERS WHERE EMAIL=email;", nativeQuery = true)
    List<UserEntity> findByEmail(@Param("email") String email, Pageable pageable);
}