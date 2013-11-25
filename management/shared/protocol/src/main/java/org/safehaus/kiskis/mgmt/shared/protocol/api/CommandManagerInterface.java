package org.safehaus.kiskis.mgmt.shared.protocol.api;

import java.util.List;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 10:40 PM
 */
public interface CommandManagerInterface {

//    List<Command> getCommandList(Agent agent);
    void executeCommand(Command command);

    void addListener(CommandListener listener);

    void removeListener(CommandListener listener);

    public List<Request> getCommands();

    public List<Response> getResponses();

    public void saveResponse(Response response);

    public String saveTask(Task task);

    public List<Task> getTasks();
}
