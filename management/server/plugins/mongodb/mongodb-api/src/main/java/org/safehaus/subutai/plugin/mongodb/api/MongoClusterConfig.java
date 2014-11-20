/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;


/**
 * Holds a single mongo cluster configuration settings
 */
public interface MongoClusterConfig extends ConfigBase
{

    public static final String PRODUCT_KEY = "MongoDB";
    public static final String PRODUCT_NAME = "mongo";

    String getTemplateName();

    int getNumberOfConfigServers();

    int getNumberOfRouters();

    int getNumberOfDataNodes();

    int getDataNodePort();

    int getRouterPort();

    String getDomainName();

    String getReplicaSetName();

    int getCfgSrvPort();

    void setConfigServers( Set<MongoConfigNode> configServers );

    void setRouterServers( Set<MongoRouterNode> routers );

    void setDataNodes( Set<MongoDataNode> dataNodes );

    Set<MongoConfigNode> getConfigServers();

    Set<MongoNode> getAllNodes();

    Set<MongoDataNode> getDataNodes();

    UUID getEnvironmentId();

    void addNode( MongoNode mongoNode, NodeType nodeType );

    void setNumberOfRouters( int i );

    Set<MongoRouterNode> getRouterServers();

    NodeType getNodeType( MongoNode node );

    void setNumberOfConfigServers( int i );

    void setCfgSrvPort( int i );

    void setRouterPort( int i );

    void setDataNodePort( int i );

    void setDomainName( String value );

    void setClusterName( String clasterName );

    void setNumberOfDataNodes( int value );

    void setReplicaSetName( String value );

    MongoDataNode findPrimaryNode() throws MongoException;

    MongoNode findNode( String lxcHostname );

    void setEnvironmentId( UUID id );
}
