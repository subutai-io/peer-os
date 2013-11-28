package org.safehaus.kiskis.mgmt.shared.protocol.api;

import java.util.List;
import java.util.UUID;

import org.safehaus.kiskis.mgmt.shared.protocol.*;
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

    public Response getResponse(UUID taskuuid, Integer requestSequenceNumber);

    public void saveResponse(Response response);

    public String saveTask(Task task);

    public List<Task> getTasks();

    public boolean truncateTables();

    public boolean saveClusterData(ClusterData cluster);
}
