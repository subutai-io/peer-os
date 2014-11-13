package org.safehaus.subutai.plugin.mongodb.api;


public interface MongoDataNode extends MongoNode
{
    public void setReplicaSetName( String replicaSetName ) throws MongoException;

    public void start() throws MongoException;

    public String getPrimaryNodeName( String domainName ) throws MongoException;

    public void registerSecondaryNode( MongoDataNode newDataNodeAgent, int dataNodePort, String domainName )
            throws MongoException;
}
