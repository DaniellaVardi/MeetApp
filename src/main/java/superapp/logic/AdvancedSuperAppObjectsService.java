package superapp.logic;

import superapp.boundaries.object.SuperAppObjectBoundary;
import superapp.boundaries.object.SuperAppObjectIdBoundary;

import java.util.List;

public interface AdvancedSuperAppObjectsService extends SuperAppObjectsService {
    public SuperAppObjectBoundary updateObject(String objectSuperapp, String internalObjectId, SuperAppObjectBoundary update,
                                               String userSuperapp,String email);
    public void bindNewChild(String parentSuperapp, String parentObjectId, SuperAppObjectIdBoundary newChild,String userSuperapp,String email);

    public List<SuperAppObjectBoundary> getAllObjects(String userSuperapp, String email, int size , int page);

    public List<SuperAppObjectBoundary> getChildren(String objectSuperapp, String internalObjectId,String userSuperapp, String email, int size, int page);

    public List<SuperAppObjectBoundary> getParents(String objectSuperapp, String internalObjectId,String userSuperapp, String email, int size, int page);

    public List<SuperAppObjectBoundary> SearchObjectsByType(String type,String userSuperapp, String email, int size, int page);

    public List<SuperAppObjectBoundary> SearchObjectsByExactAlias(String alias, String userSuperapp, String email, int size, int page);

    public SuperAppObjectBoundary getSpecificObject(String objectSuperapp, String internalObjectId, String userSuperapp, String email);

    public List<SuperAppObjectBoundary> SearchObjectsByExactAliasContainingText(String text, String userSupperapp, String email, int size, int page);

    public void deleteAllObjects(String userSuperapp, String email);
    }
