package org.safehaus.subutai.plugin.mongodb.api;


public interface MongoDataNode extends MongoNode
{
    public void setReplicaSetName( String replicaSetName ) throws MongoException;

    public String getPrimaryNodeName() throws MongoException;

    public void registerSecondaryNode( MongoDataNode dataNode ) throws MongoException;

    void initiateReplicaSet() throws MongoException;

    void unRegisterSecondaryNode( MongoDataNode dataNode ) throws MongoException;
}
