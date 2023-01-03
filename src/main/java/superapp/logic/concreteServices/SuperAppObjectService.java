package superapp.logic.concreteServices;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import superapp.boundaries.object.SuperAppObjectBoundary;
import superapp.boundaries.object.SuperAppObjectIdBoundary;
import superapp.boundaries.user.UserIdBoundary;
import superapp.converters.SuperAppObjectConverter;
import superapp.dal.IdGeneratorRepository;
import superapp.dal.SuperAppObjectEntityRepository;
import superapp.data.IdGeneratorEntity;
import superapp.data.SuperAppObjectEntity;
import superapp.data.SuperappObjectPK;
import superapp.logic.AbstractService;
import superapp.logic.SuperAppObjectsService;
import superapp.util.exceptions.CannotProcessException;
import superapp.util.exceptions.InvalidInputException;
import superapp.util.exceptions.NotFoundException;
import superapp.util.EmailChecker;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static superapp.data.ObjectTypes.isValidObjectType;

@Service
public class SuperAppObjectService extends AbstractService implements SuperAppObjectsService {
    private SuperAppObjectEntityRepository objectRepository;
    private IdGeneratorRepository idGenerator;
    private SuperAppObjectConverter converter;
    private ServiceHandler serviceHandler;

    @Autowired
    public SuperAppObjectService(SuperAppObjectConverter converter,
                                 SuperAppObjectEntityRepository objectRepository,
                                 IdGeneratorRepository idGenerator, ServiceHandler serviceHandler) {
        this.converter = converter;
        this.objectRepository = objectRepository;
        this.idGenerator = idGenerator;
        this.serviceHandler = serviceHandler;
    }

    @Override
    @Transactional
    public SuperAppObjectBoundary createObject(SuperAppObjectBoundary object) {
        String alias = object.getAlias();
        String type = object.getType();
        if (alias == null || type == null || alias.isBlank() || type.isBlank())
            throw new InvalidInputException("Object alias and/or type must be specified");

        UserIdBoundary createdBy = object.getCreatedBy().getUserId();
        if (createdBy == null ||
                createdBy.getEmail() == null ||
                createdBy.getSuperapp() == null ||
                createdBy.getSuperapp().isEmpty() ||
                !EmailChecker.isValidEmail(createdBy.getEmail()))
            throw new InvalidInputException("Invalid creating user details");

        Boolean active = object.getActive();
        if (active == null)
            active = false;

        IdGeneratorEntity helper = this.idGenerator.save(new IdGeneratorEntity());
        String objectId = helper.getId().toString();
        this.idGenerator.delete(helper);

        object.setObjectId(new SuperAppObjectIdBoundary(this.superappName, objectId));
        object.setActive(active);
        object.setCreationTimestamp(new Date());
        // handleObjectByType will handle any unknown object type by 400 - Bad request.
        serviceHandler.handleObjectByType(object);
        this.objectRepository.save(converter.toEntity(object));
        return object;
    }


    @Override
    @Transactional
    public SuperAppObjectBoundary updateObject(String objectSuperapp,
                                               String internalObjectId,
                                               SuperAppObjectBoundary update) {
        Optional<SuperAppObjectEntity> objectO =
                this.objectRepository.findById(new SuperappObjectPK(objectSuperapp, internalObjectId));
        if (objectO.isEmpty())
            throw new NotFoundException("Unknown object");
        SuperAppObjectEntity objectE = objectO.get();
        Map<String, Object> newDetails = update.getObjectDetails();
        Boolean newActive = update.getActive();
        String newType = update.getType();
        String newAlias = update.getAlias();

        if (newDetails != null)
            objectE.setObjectDetails(this.converter.detailsToString(newDetails));
        if (newActive != null)
            objectE.setActive(newActive);

        if (newType != null) {
            if (newType.isBlank())
                throw new InvalidInputException("Object alias and/or type must be specified");
            else if (!isValidObjectType(newType))
                throw new InvalidInputException("Unknown object type");
            else
                objectE.setType(newType);
        }

        if (newAlias != null) {
            if (newAlias.isBlank())
                throw new InvalidInputException("Object alias and/or type must be specified");
            else
                objectE.setAlias(newAlias);
        }
        SuperAppObjectBoundary result = this.converter.toBoundary(objectE);
        /*
            handleObjectByType will handle any unknown object type by 400 - Bad request.
            if object details after update doesn't fit into miniapp restrictions, an exception will be thrown as well
        */
        serviceHandler.handleObjectByType(result);
        this.objectRepository.save(objectE);
        return result;
    }

    @Override
    @Transactional
    public void bindNewChild(String parentSuperapp, String parentObjectId, SuperAppObjectIdBoundary newChild) {
        SuperAppObjectEntity parent = this.objectRepository
                .findById(new SuperappObjectPK(parentSuperapp, parentObjectId))
                .orElseThrow(() -> new NotFoundException("Cannot find parent object"));
        SuperAppObjectEntity child = this.objectRepository
                .findById(this.converter.idToEntity(newChild))
                .orElseThrow(() -> new NotFoundException("Cannot find children object"));

       if (parent.addChild(child) && child.addParent(parent)) {
           this.objectRepository.save(parent);
           this.objectRepository.save(child);
       } else
           throw new CannotProcessException("Failed to update parent or child object");
    }

    @Override
    @Transactional(readOnly = true)
    public SuperAppObjectBoundary getSpecificObject(String objectSuperapp, String internalObjectId) {
        Optional<SuperAppObjectEntity> objectE =
                this.objectRepository.findById(new SuperappObjectPK(objectSuperapp, internalObjectId));
        if (objectE.isEmpty())
            throw new NotFoundException("Object does not exist");

        return this.converter.toBoundary(objectE.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuperAppObjectBoundary> getChildren(String objectSuperapp, String internalObjectId) {
        SuperAppObjectEntity parent = this.objectRepository
                .findById(new SuperappObjectPK(objectSuperapp, internalObjectId))
                .orElseThrow(() -> new NotFoundException("Cannot find parent object"));

        return parent
                .getChildren()
                .stream()
                .map(this.converter::toBoundary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuperAppObjectBoundary> getParents(String objectSuperapp, String internalObjectId) {
        SuperAppObjectEntity object = this.objectRepository
                .findById(new SuperappObjectPK(objectSuperapp, internalObjectId))
                .orElseThrow(() -> new NotFoundException("Cannot find requested object"));

        return object
                .getParents()
                .stream()
                .map(this.converter::toBoundary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuperAppObjectBoundary> getAllObjects() {
        Iterable<SuperAppObjectEntity> objects = this.objectRepository.findAll();
        return StreamSupport
                .stream(objects.spliterator() , false)
                .map(this.converter::toBoundary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllObjects() { this.objectRepository.deleteAll(); }
}
