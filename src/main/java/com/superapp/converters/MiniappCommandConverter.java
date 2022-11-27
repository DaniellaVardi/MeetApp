package com.superapp.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superapp.boundaries.command.MiniAppCommandBoundary;
import com.superapp.boundaries.command.MiniAppCommandIdBoundary;
import com.superapp.boundaries.command.ObjectIdBoundary;
import com.superapp.boundaries.command.user.UserIdBoundary;
import com.superapp.data.MiniAppCommandEntity;
import com.superapp.util.wrappers.ObjectIdWrapper;
import com.superapp.util.wrappers.UserIdWrapper;

import java.util.Map;

public class MiniappCommandConverter {
    private ObjectMapper jackson;
    public MiniappCommandConverter(){
        this.jackson = new ObjectMapper();

    }
    public MiniAppCommandEntity toEntity(MiniAppCommandBoundary miniApp) {
        MiniAppCommandEntity rv = new MiniAppCommandEntity();
        rv.setSuperApp(miniApp.getCommandId().getSuperApp());
        rv.setMiniApp(miniApp.getCommandId().getMiniApp());
        rv.setCommand(miniApp.getCommand());
        rv.setInvocationTimeStamp(miniApp.getInvocationTimeStamp());
        rv.setInternalObjectId(((ObjectIdWrapper)miniApp.getTargetObject()).getObjectId().getInternalObjectId());
        rv.setEmail(((UserIdWrapper)miniApp.getInvokedBy()).getUserId().getEmail());
        rv.setCommandAttributes(toEntityAsString((Map<String, Object>) miniApp.getCommandAttributes()));
        return rv;
    }
    public String toEntityAsString(Map<String, Object> attributes) {
        try {
            return this.jackson
                    .writeValueAsString(attributes);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private Map<String, Object> toBoundaryAsMap(String attributes) {
        try {
            return (Map<String, Object>)this.jackson
                    .readValue(attributes, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public MiniAppCommandBoundary toBoundary(MiniAppCommandEntity miniappEntity) {
        MiniAppCommandBoundary rv = new MiniAppCommandBoundary();
        rv.setCommandId(new MiniAppCommandIdBoundary(miniappEntity.getSuperApp(),miniappEntity.getInternalCommandId()));
        rv.setCommand(miniappEntity.getCommand());
        rv.setCommandAttributes(toBoundaryAsMap(miniappEntity.getCommandAttributes()));
        rv.setInvokedBy(new UserIdWrapper(new UserIdBoundary(miniappEntity.getSuperApp(), miniappEntity.getEmail())));
        rv.setTargetObject(new ObjectIdWrapper(new ObjectIdBoundary(miniappEntity.getInternalObjectId())));
        rv.setInvocationTimeStamp(miniappEntity.getInvocationTimeStamp());
        return rv;
    }
}
