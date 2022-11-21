package application.boundaries.command;

import application.boundaries.user.UserIdBoundary;
import application.util.wrappers.ObjectIdWrapper;
import application.util.wrappers.UserIdWrapper;

import boundaries.user.UserBoundary;
import org.apache.catalina.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CommandBoundary {

    private CommandIdBoundary commandId ;
    private String command;
    private ObjectIdWrapper targetObject;
    private Date invocationTimeStamp;
    private UserIdWrapper invokedBy;
    private Map<String, Object> commandAttributes;

    public CommandBoundary() { }

    public CommandBoundary(CommandIdBoundary commandId, String command,
                           ObjectIdBoundary targetObject, UserIdBoundary invokedBy,
                           Map<String, Object> commandAttributes)
    {
        this();
        this.commandId = commandId;
        this.command = command;
        this.commandAttributes = commandAttributes;
        this.invocationTimeStamp = new Date();
        this.targetObject = new ObjectIdWrapper(targetObject);
        this.invokedBy = new UserIdWrapper(invokedBy);
    }
    public static CommandBoundary[] getNcommandBoundries(int n ){
        Map<String,Object> commandAttributes;
        String commandName = "CommandName num :";
        CommandBoundary[] commandArray = new CommandBoundary[n];
        for(int i=0; i<n ;i++){
            invokedBy = new HashMap<String,Object>();
            invokedBy.put(commandName,UserBoundary.getNRandomUsers(1)[0].getUserId());
            commandAttributes = new HashMap<String,Object>();
            commandAttributes.put("key "+i,i);
            commandArray[i] = new CommandBoundary(new CommandIdBoundary(),commandName+i,new ObjectIdBoundary(),new UserIdBoundary(),commandAttributes);
        }

        return commandArray;
    }

    public CommandIdBoundary getCommandId() {
        return commandId;
    }

    public void setCommandId(CommandIdBoundary commandId) {
        this.commandId = commandId;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(ObjectIdWrapper targetObject) { this.targetObject = targetObject; }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Date getInvocationTimeStamp() {
        return invocationTimeStamp;
    }

    public void setInvocationTimeStamp(Date invocationTimeStamp) {
        this.invocationTimeStamp = invocationTimeStamp;
    }

    public Object getInvokedBy() {
        return invokedBy;
    }

    public void setInvokedBy(UserIdWrapper invokedBy) { this.invokedBy = invokedBy; }

    public Object getCommandAttributes() {
        return commandAttributes;
    }

    public void setCommandAttributes(Map<String, Object> commandAttributes) {
        this.commandAttributes = commandAttributes;
    }

    @Override
    public String toString() {
        return "CommandBoundary{" +
                "command='" + command + '\'' +
                ", targetObject=" + targetObject +
                ", invocationTimeStamp=" + invocationTimeStamp +
                ", invokedBy=" + invokedBy +
                ", commandAttributes=" + commandAttributes +
                '}';
    }

}
