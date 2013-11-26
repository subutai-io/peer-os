/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.api;

import java.util.List;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 *
 * @author dilshat
 */
public interface PersistenceInterface {

//    //Commands section
//    List<Command> getCommandList(Agent agent);
    boolean saveCommand(Command command);

    List<Request> getRequests(String taskuuid);

    List<Response> getResponses(String taskuuid);

    boolean saveResponse(Response response);

    //Agents section
    List<Agent> getAgentList();

    boolean saveAgent(Agent agent);

    boolean updateAgent(Agent agent);

    String saveTask(Task task);
    
    List<Task> getTasks();

    public boolean truncateTables();
}
