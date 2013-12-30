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

    /**
     * Agent
     */
    public Agent getAgent(UUID uuid);

    public boolean saveAgent(Agent agent);

    public boolean removeAgent(String transportId);

    /**
     * Heartbeat, LXC, Physical
     */
    public List<Agent> getRegisteredAgents(long freshness);

    public List<Agent> getRegisteredLxcAgents(long freshness);

    public List<Agent> getUnknownChildLxcAgents(long freshness);

    public List<Agent> getRegisteredChildLxcAgents(Agent parent, long freshness);

    public List<Agent> getRegisteredPhysicalAgents(long freshness);

    public Agent getRegisteredLxcAgentByHostname(String hostname, long freshness);

    public Agent getRegisteredPhysicalAgentByHostname(String hostname, long freshness);

    public List<Agent> getAgentsByHeartbeat(long from, long to);

    /**
     * Commands
     */
    public boolean saveCommand(Command command);

    public List<Request> getRequests(UUID taskuuid);

    public Integer getResponsesCount(UUID taskUuid);

    public Integer getSuccessfullResponsesCount(UUID taskUuid);

    public List<Response> getResponses(UUID taskuuid, Integer requestSequenceNumber);

    public boolean saveResponse(Response response);

    /**
     * Task
     */
    public String saveTask(Task task);

    public List<Task> getTasks();

    public Task getTask(UUID uuid);

    /**
     * Utils
     */
    public boolean truncateTables();

    /**
     * Cassandra
     */
    public boolean saveCassandraClusterInfo(CassandraClusterInfo cluster);

    public List<CassandraClusterInfo> getCassandraClusterInfo();

    public CassandraClusterInfo getCassandraClusterInfo(String clusterName);

    public boolean deleteCassandraClusterInfo(UUID uuid);

    /**
     *
     */
    public boolean saveHadoopClusterInfo(HadoopClusterInfo cluster);

    public List<HadoopClusterInfo> getHadoopClusterInfo();

    public HadoopClusterInfo getHadoopClusterInfo(String clusterName);

    //=========MONGO============================================================
    public boolean saveMongoClusterInfo(MongoClusterInfo clusterInfo);

    public List<MongoClusterInfo> getMongoClustersInfo();

    public MongoClusterInfo getMongoClusterInfo(String clusterName);

    public boolean deleteMongoClusterInfo(String clusterName);

}
