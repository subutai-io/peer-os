package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import java.util.Iterator;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.Commands;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;


public class ClusterOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{
    private String clusterName;
    private OperationType operationType;


    public ClusterOperationHandler( final ElasticsearchImpl manager, final String clusterName,
                                    final OperationType operationType )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        this.operationType = operationType;
        trackerOperation = manager.getTracker().createTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    public void run()
    {
        ElasticsearchClusterConfiguration config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Iterator iterator = environment.getContainers().iterator();

        ContainerHost host;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();

            if ( host != null )
            {
                try
                {
                    CommandResult result = null;
                    switch ( operationType )
                    {
                        case START:
                            result = host.execute( new RequestBuilder( Commands.startCommand ) );
                            break;
                        case STOP:
                            result = host.execute( new RequestBuilder( Commands.stopCommand ) );
                            break;
                        case STATUS:
                            result = host.execute( new RequestBuilder( Commands.statusCommand ) );
                            break;
                    }
                    NodeOperationHandler.logStatusResults( trackerOperation, result );
                }
                catch ( CommandException e )
                {
                    trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
                }
            }
            else
            {
                trackerOperation.addLogFailed( String.format( "No Container with ID %s", host.getAgent().getUuid() ) );
                return;
            }
        }
    }
}
