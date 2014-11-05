package org.safehaus.subutai.plugin.pig.impl.handler;


import java.util.Iterator;
import java.util.UUID;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.pig.api.PigConfig;
import org.safehaus.subutai.plugin.pig.api.SetupType;
import org.safehaus.subutai.plugin.pig.impl.Commands;
import org.safehaus.subutai.plugin.pig.impl.PigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DestroyClusterOperationHandler extends AbstractOperationHandler<PigImpl>
{

    private static final Logger LOG = LoggerFactory.getLogger( DestroyClusterOperationHandler.class.getName() );

    public DestroyClusterOperationHandler( PigImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( PigConfig.PRODUCT_KEY,
                String.format( "Destroying %s ", clusterName ) );
    }


    @Override
    public void run()
    {
        TrackerOperation po = trackerOperation;
        PigConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        for ( ContainerHost host : config.getNodes() )
        {
            String hostName = host.getHostname();
            if ( hostName == null )
            {
                po.addLogFailed( String.format( "Node %s is not connected\nOperation aborted", hostName ) );
                return;
            }
        }

        boolean ok = false;
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            ok = uninstall( config );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            //ok = destroyNodes( config );
        }
        else
        {
            po.addLog( "Undefined setup type" );
        }

        if ( ok )
        {
            po.addLog( "Updating db..." );
            manager.getPluginDao().deleteInfo( PigConfig.PRODUCT_KEY, config.getClusterName() );
            po.addLogDone( "Cluster info deleted from DB\nDone" );
        }
        else
        {
            po.addLogFailed( "Failed to destroy cluster" );
        }
    }


    private boolean uninstall( PigConfig config )
    {
        TrackerOperation po = trackerOperation;
        po.addLog( "Uninstalling Pig..." );

        for ( Iterator<ContainerHost> it = config.getNodes().iterator(); it.hasNext(); )
        {
            ContainerHost host = it.next();

            CommandResult result = null;
            try
            {
                result = host.execute( new RequestBuilder( Commands.uninstallCommand ) );
                if ( !result.hasSucceeded() )
                {
                    po.addLog( result.getStdErr() );
                    po.addLogFailed( "Uninstallation failed" );
                    return false;
                }
            }
            catch ( CommandException e )
            {
                LOG.error( e.getMessage(), e );
            }
        }
        return true;
    }



//    private boolean destroyNodes( PigConfig config )
//    {
//
//        trackerOperation.addLog( "Destroying node(s)..." );
//        try
//        {
//            manager.getContainerManager().clonesDestroy( config.getNodes() );
//            trackerOperation.addLog( "Destroying node(s) completed" );
//            return true;
//        }
//        catch ( LxcDestroyException ex )
//        {
//            trackerOperation.addLog( "Failed to destroy node(s): " + ex.getMessage() );
//            return false;
//        }
//    }
    public ContainerHost getHostByUUID(UUID agentUUID, PigConfig config)
    {
        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Iterator iterator = environment.getContainers().iterator();
        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getId().equals( agentUUID ) )
            {
                break;
            }
        }
        return host;
    }
}

