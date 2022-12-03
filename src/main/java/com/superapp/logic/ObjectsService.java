package com.superapp.logic;

import com.superapp.boundaries.object.ObjectBoundary;

import java.util.List;

public interface ObjectsService {
    ObjectBoundary createObject(ObjectBoundary object);

    ObjectBoundary updateObject(String objectSuperApp, String InternalObjectId, ObjectBoundary update);

    ObjectBoundary getSpecificObject(String objectSuperApp, String internalObjectId);

    List<ObjectBoundary> getAllObjects();

    void deleteAllObjects();
}
