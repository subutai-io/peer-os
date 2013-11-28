/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.ClusterData;
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
    boolean saveCommand(Command command);

    List<Request> getRequests(UUID taskuuid);

    List<Response> getResponses(UUID taskuuid, Integer requestSequenceNumber);

    boolean saveResponse(Response response);

    Set<Agent> getRegisteredAgents(long freshness);

    Set<Agent> getRegisteredLxcAgents(long freshness);

    Set<Agent> getRegisteredPhysicalAgents(long freshness);

    Set<Agent> getAgentsByHeartbeat(long from, long to);

    boolean saveAgent(Agent agent);

//    boolean updateAgent(Agent agent);
//
//    boolean removeAgent(Agent agent);

    String saveTask(Task task);

    List<Task> getTasks();

    public boolean truncateTables();

    public boolean saveClusterData(ClusterData cluster);
}
