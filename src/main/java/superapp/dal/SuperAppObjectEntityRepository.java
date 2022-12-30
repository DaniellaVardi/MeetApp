package superapp.dal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import superapp.data.SuperAppObjectEntity;
import superapp.data.SuperappObjectPK;


@Repository
public interface SuperAppObjectEntityRepository extends CrudRepository<SuperAppObjectEntity, SuperappObjectPK> {

}