package org.safehaus.subutai.plugin.lucene.impl.handler;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.lucene.api.LuceneConfig;
import org.safehaus.subutai.plugin.lucene.impl.LuceneImpl;

import java.util.Iterator;

/**
 * Created by ebru on 09.11.2014.
 */
public class NodeOperationHandler extends AbstractOperationHandler<LuceneImpl, LuceneConfig> {

    private String clusterName;
    private String hostname;
    private NodeOperationType operationType;

    public NodeOperationHandler( final LuceneImpl manager, final LuceneConfig config, final String hostname,
                                 NodeOperationType operationType )
    {
        super( manager, config );
        this.hostname = hostname;
        this.clusterName = clusterName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker()
                .createTrackerOperation( LuceneConfig.PRODUCT_KEY,
                        String.format( "Creating %s tracker object...", clusterName ) );
    }


    @Override
    public void run()
    {
        LuceneConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Iterator iterator = environment.getContainers().iterator();
        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getHostname().equals( hostname ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", hostname ) );
            return;
        }


        CommandResult result = null;
        switch ( operationType )
        {
            // TODO add and destroy node
        }



    }

}
