package application.entities;

import application.util.wrappers.UserIdWrapper;

import java.util.Date;
import java.util.Map;

public class ObjectEntity {

    private Long FK_objectId;
    private String type;
    private String alias;
    private boolean active;
    private Date creationTimeStamp;
    private UserIdWrapper createdBy;
    private Map<String, Object> objectDetails;

    public ObjectEntity() {
    }

    public Long getObjectId() {
        return FK_objectId;
    }

    public void setObjectId(Long FK_objectId) {
        this.FK_objectId = FK_objectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreationTimeStamp() {
        return creationTimeStamp;
    }

    public void setCreationTimeStamp(Date creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
    }
    public UserIdWrapper getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserIdWrapper createdBy) {
        this.createdBy = createdBy;
    }

    public Map<String, Object> getObjectDetails() {
        return objectDetails;
    }

    public void setObjectDetails(Map<String, Object> objectDetails) {
        this.objectDetails = objectDetails;
    }

    @Override
    public String toString() {
        return "ObjectBoundary{" +
                "objectId='" + FK_objectId + '\'' +
                ", type='" + type + '\'' +
                ", alias='" + alias + '\'' +
                ", active=" + active +
                ", creationTimeStamp=" + creationTimeStamp +
                ", createBy=" + createdBy +
                ", objectDetails=" + objectDetails +
                '}';
    }

}
