package superapp.controllers;

import superapp.boundaries.command.MiniAppCommandBoundary;
import superapp.boundaries.command.MiniAppCommandIdBoundary;
import superapp.boundaries.object.ObjectIdBoundary;
import superapp.boundaries.user.UserIdBoundary;
import superapp.logic.concreteServices.MiniAppCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import superapp.util.SuperappChecker;

import java.util.Date;

@RestController
public class MiniAppObjectsController {

    private MiniAppCommandService miniAppCommandService;
    private final SuperappChecker checker = new SuperappChecker();

    @Autowired
    public void MiniAppCommandService(MiniAppCommandService miniappservice) {
        this.miniAppCommandService = miniappservice;
    }

    @RequestMapping(
            path= {"/superapp/miniapp/{miniAppName}"},
            method = {RequestMethod.POST},
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public Object invokeMiniAppCommand (@RequestBody MiniAppCommandBoundary command,
                                        @PathVariable("miniAppName") String miniAppName)
    {
        MiniAppCommandIdBoundary id = command.getCommandId();
        UserIdBoundary invokedBy = command.getInvokedBy().getUserId();
        ObjectIdBoundary targetObject = command.getTargetObject().getObjectId();
        if (id != null && !checker.isValidSuperapp(id.getSuperapp()) ||
                invokedBy != null && !checker.isValidSuperapp(invokedBy.getSuperapp()) ||
                targetObject != null && !checker.isValidSuperapp(targetObject.getSuperapp()))
            throw new RuntimeException("Incorrect superapp");

        command.setInvocationTimestamp(new Date());
        command.getCommandId().setMiniapp(miniAppName); // TODO: check if miniapp exists
        return this.miniAppCommandService.invokeCommand(command);
    }
}
