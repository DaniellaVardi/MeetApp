package application.controllers;

import application.boundaries.command.CommandBoundary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
public class MiniAppObjectsController {

    @RequestMapping(
            path= {"/superapp/miniapp/{miniAppName}"},
            method = {RequestMethod.POST},
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public Object invokeMiniAppCommand (@RequestBody CommandBoundary command,
                                        @PathVariable("miniAppName") String miniAppName)
    {
        command.setInvocationTimeStamp(new Date());
        return command;
    }
}
