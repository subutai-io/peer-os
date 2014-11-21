/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.api;


import java.util.Set;
import java.util.UUID;

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

    void setNumberOfConfigServers( int i );

    int getNumberOfRouters();

    void setNumberOfRouters( int i );

    int getNumberOfDataNodes();

    void setNumberOfDataNodes( int value );

    int getDataNodePort();

    void setDataNodePort( int i );

    int getRouterPort();

    void setRouterPort( int i );

    String getDomainName();

    void setDomainName( String value );

    String getReplicaSetName();

    void setReplicaSetName( String value );

    int getCfgSrvPort();

    void setCfgSrvPort( int i );

    Set<MongoConfigNode> getConfigServers();

    void setConfigServers( Set<MongoConfigNode> configServers );

    Set<MongoNode> getAllNodes();

    Set<MongoDataNode> getDataNodes();

    void setDataNodes( Set<MongoDataNode> dataNodes );

    UUID getEnvironmentId();

    void setEnvironmentId( UUID id );

    void addNode( MongoNode mongoNode, NodeType nodeType );

    Set<MongoRouterNode> getRouterServers();

    void setRouterServers( Set<MongoRouterNode> routers );

    NodeType getNodeType( MongoNode node );

    void setClusterName( String clasterName );

    MongoDataNode findPrimaryNode() throws MongoException;

    MongoNode findNode( String lxcHostname );
}
