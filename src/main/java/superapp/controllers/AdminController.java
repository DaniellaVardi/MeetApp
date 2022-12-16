package superapp.controllers;

import superapp.boundaries.user.UserBoundary;
import superapp.boundaries.command.MiniAppCommandBoundary;
import superapp.logic.UsersService;
import superapp.logic.concreteServices.MiniAppCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import superapp.logic.concreteServices.ObjectService;

@RestController
public class AdminController {

    private UsersService usersService;
    private MiniAppCommandService miniappService;
    private ObjectService objectService;

    @Autowired
    public void setMessageService(UsersService usersService) {
        this.usersService = usersService;
    }

    @Autowired
    public void setMiniAppCommandService(MiniAppCommandService MiniAppCommandService) {
        this.miniappService = MiniAppCommandService;
    }

    @Autowired
    public void setObjectService(ObjectService objectService) { this.objectService = objectService; }

    @RequestMapping(
            path = {"/superapp/admin/users"},
            method = {RequestMethod.GET},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public UserBoundary[] getAllUsers() {
        return this.usersService.getAllUsers().toArray(new UserBoundary[0]);
    }

    @RequestMapping(
            path = {"/superapp/admin/miniapp"},
            method = {RequestMethod.GET},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public MiniAppCommandBoundary[] exportMiniAppsCommands() {
        return this.miniappService.getALlCommands()
                .toArray(new MiniAppCommandBoundary[0]);
    }

    @RequestMapping(
            path= {"/superapp/admin/miniapp/{miniAppName}"},
            method = {RequestMethod.GET},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public MiniAppCommandBoundary[] exportSpecificMiniAppsCommands(@PathVariable("miniAppName") String miniAppName) {
        return this.miniappService.getAllMiniAppCommands(miniAppName)
                .toArray(new MiniAppCommandBoundary[0]);
    }

    @RequestMapping(
                path= {"/superapp/admin/users"},
                method = {RequestMethod.DELETE})
    public void deleteUsers() { this.usersService.deleteAllUsers(); }

    @RequestMapping(
            path= {"/superapp/admin/objects"},
            method = {RequestMethod.DELETE})
    public void deleteObjects() { this.objectService.deleteAllObjects(); }

    @RequestMapping(
            path= {"/superapp/admin/miniapp"},
            method = {RequestMethod.DELETE})
    public void deleteMiniApp() { this.miniappService.deleteALlCommands(); }
}
