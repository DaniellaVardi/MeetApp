package superapp.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import superapp.boundaries.object.SuperAppObjectIdBoundary;
import superapp.boundaries.object.SuperAppObjectBoundary;
import superapp.data.SuperAppObjectEntity;
import superapp.data.SuperAppObjectEntity.SuperAppObjectId;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SuperAppObjectConverter {

    private ObjectMapper mapper;

    public SuperAppObjectConverter() { this.mapper = new ObjectMapper(); }

    public SuperAppObjectEntity toEntity(SuperAppObjectBoundary obj) {
        SuperAppObjectEntity objEntity = new SuperAppObjectEntity();
        objEntity.setObjectId(obj.getObjectId().getInternalObjectId());
        objEntity.setSuperapp(obj.getObjectId().getSuperapp());
        objEntity.setActive(obj.getActive());
        objEntity.setAlias(obj.getAlias());
        objEntity.setObjectDetails(this.detailsToString(obj.getObjectDetails()));
        objEntity.setType(obj.getType());
        objEntity.setCreatedBy(obj.getCreatedBy().getUserId());
        objEntity.setCreationTimestamp(obj.getCreationTimestamp());

        return objEntity;
    }

    public SuperAppObjectBoundary toBoundary(SuperAppObjectEntity obj) {
        SuperAppObjectBoundary objBoundary = new SuperAppObjectBoundary();
        objBoundary.setObjectId(new SuperAppObjectIdBoundary(obj.getSuperapp(), obj.getObjectId()));
        objBoundary.setActive(obj.getActive());
        objBoundary.setAlias(obj.getAlias());
        objBoundary.setObjectDetails(this.detailsToMap(obj.getObjectDetails()));
        objBoundary.setType(obj.getType());
        objBoundary.setCreatedBy(obj.getCreatedBy());
        objBoundary.setCreationTimestamp(obj.getCreationTimestamp());

        return objBoundary;
    }

    public SuperAppObjectId idToEntity(SuperAppObjectIdBoundary obj) {
        return new SuperAppObjectId(obj.getSuperapp(), obj.getInternalObjectId());
    }

    public SuperAppObjectIdBoundary idToBoundary(SuperAppObjectId obj) {
        return new SuperAppObjectIdBoundary(obj.getSuperapp(), obj.getObjectId());
    }

    public String detailsToString(Map<String, Object> objectDetails) {
        try {
            return mapper.writeValueAsString(objectDetails);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> detailsToMap(String details) {
        try {
            return (Map<String, Object>)this.mapper.readValue(details, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
