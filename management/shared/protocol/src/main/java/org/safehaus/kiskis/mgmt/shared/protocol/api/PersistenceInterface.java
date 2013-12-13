/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.api;

import org.safehaus.kiskis.mgmt.shared.protocol.*;

import java.util.List;
import java.util.UUID;

/**
 * @author dilshat
 */
public interface PersistenceInterface {

    //    //Commands section
    boolean saveCommand(Command command);

    List<Request> getRequests(UUID taskuuid);

    Integer getResponsesCount(UUID taskUuid);

    List<Response> getResponses(UUID taskuuid, Integer requestSequenceNumber);

    boolean saveResponse(Response response);

    List<Agent> getRegisteredAgents(long freshness);

    List<Agent> getRegisteredLxcAgents(long freshness);

    List<Agent> getUnknownChildLxcAgents(long freshness);

    List<Agent> getRegisteredChildLxcAgents(Agent parent, long freshness);

    List<Agent> getRegisteredPhysicalAgents(long freshness);

    Agent getRegisteredLxcAgentByHostname(String hostname, long freshness);

    Agent getRegisteredPhysicalAgentByHostname(String hostname, long freshness);

    List<Agent> getAgentsByHeartbeat(long from, long to);

    Agent getAgent(UUID uuid);

    boolean saveAgent(Agent agent);

    String saveTask(Task task);

    List<Task> getTasks();

    Task getTask(UUID uuid);

    public boolean truncateTables();

    public boolean saveCassandraClusterInfo(CassandraClusterInfo cluster);

    public List<CassandraClusterInfo> getCassandraClusterInfo();

    public CassandraClusterInfo getCassandraClusterInfo(String clusterName);

    public boolean saveHadoopClusterInfo(HadoopClusterInfo cluster);

    public List<HadoopClusterInfo> getHadoopClusterInfo();

    public HadoopClusterInfo getHadoopClusterInfo(String clusterName);
}
