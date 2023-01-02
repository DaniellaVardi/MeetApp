package superapp.logic.concreteServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import superapp.boundaries.object.SuperAppObjectBoundary;
import superapp.boundaries.user.UserIdBoundary;
import superapp.logic.MiniAppServiceHandler;
import superapp.util.exceptions.InvalidInputException;
import superapp.util.wrappers.SuperAppObjectIdWrapper;

import static superapp.data.ObjectTypes.isValidObjectType;

@Service
public class ServiceHandler implements MiniAppServiceHandler {
    private SplitService splitService;
    //private GrabService grabService
    //private LiftService liftService

    @Autowired
    public ServiceHandler(SplitService splitService/*,GrabService grabService , LiftService liftService*/) {
        this.splitService = splitService;
        //grabService = grabService;
        //liftService =liftService;
    }

    public void handleObjectByType(SuperAppObjectBoundary object) {
        String objectType = object.getType();
        if (!isValidObjectType(objectType))
            objectType = "";
        switch (objectType) {
            case ("TRANSACTION"), ("GROUP") -> this.splitService.handleObjectByType(object);
        }
    }

    @Override
    public Object runCommand(String miniapp,
                             SuperAppObjectIdWrapper targetObject,
                             UserIdBoundary invokedBy,
                             String commandCase) {
        switch (miniapp) {
            case ("Split") -> { return this.splitService.runCommand(miniapp, targetObject, invokedBy, commandCase); }
            case ("Grab") -> { return null;
                //this.grabService.runCommand(miniapp,targetObject,user,attributes,commandCase);
            }
            case ("Lift") -> { return null;
                //this.liftService.runCommand(miniapp,targetObject,user,attributes,commandCase);
            }
            default -> { throw new InvalidInputException("MiniApp Not Found"); }
        }
    }
}