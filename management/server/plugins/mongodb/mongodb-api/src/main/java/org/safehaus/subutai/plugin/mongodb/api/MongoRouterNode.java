package org.safehaus.subutai.plugin.mongodb.api;


import java.util.Set;


public interface MongoRouterNode extends MongoNode
{
    public void start( Set<MongoConfigNode> configServers, String domainName, int cfgSrvPort ) throws MongoException;

    public void stop() throws MongoException;
}
