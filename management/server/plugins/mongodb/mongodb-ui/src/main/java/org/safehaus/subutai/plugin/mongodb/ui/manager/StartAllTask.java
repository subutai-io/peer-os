package org.safehaus.subutai.plugin.mongodb.ui.manager;


import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoDataNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.api.MongoRouterNode;


public class StartAllTask implements Runnable
{
    private final MongoClusterConfig mongoClusterConfig;
    private final CompleteEvent completeEvent;
    private final Mongo mongo;
    private final Tracker tracker;


    public StartAllTask( Mongo mongo, Tracker tracker, MongoClusterConfig config, CompleteEvent completeEvent )
    {
        this.mongo = mongo;
        this.tracker = tracker;
        this.completeEvent = completeEvent;
        this.mongoClusterConfig = config;
    }


    public void run()
    {
        for ( MongoConfigNode configNode : mongoClusterConfig.getConfigServers() )
        {
            try
            {
                configNode.start();
            }
            catch ( MongoException e )
            {
                e.printStackTrace();
            }
        }

        for ( MongoRouterNode routerNode : mongoClusterConfig.getRouterServers() )
        {
            try
            {
                routerNode.setConfigServers( mongoClusterConfig.getConfigServers() );
                routerNode.start();
            }
            catch ( MongoException e )
            {
                e.printStackTrace();
            }
        }

        for ( MongoDataNode dataNode : mongoClusterConfig.getDataNodes() )
        {
            try
            {
                dataNode.start();
            }
            catch ( MongoException e )
            {
                e.printStackTrace();
            }
        }
        completeEvent.onComplete( NodeState.UNKNOWN );
    }
}
