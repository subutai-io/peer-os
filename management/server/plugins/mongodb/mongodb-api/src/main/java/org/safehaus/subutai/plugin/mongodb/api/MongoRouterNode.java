package org.safehaus.subutai.plugin.mongodb.api;


import java.util.Set;


public interface MongoRouterNode extends MongoNode
{

    public void registerDataNodesWithReplica( Set<MongoDataNode> dataNodes, String replicaName ) throws MongoException;

    void setConfigServers( Set<MongoConfigNode> configServers );
}
