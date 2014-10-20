package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.Set;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;


public class UninstallClusterHandler extends AbstractOperationHandler<CassandraImpl>
{

    private String clusterName;


    public UninstallClusterHandler( final CassandraImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public void run()
    {
        productOperation.addLog( "Building environment..." );
        CassandraClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        productOperation.addLog( "Destroying lxc containers" );
        try
        {
            Set<Agent> agentSet = manager.getAgentManager().returnAgentsByGivenUUIDSet( config.getNodes() );
            manager.getContainerManager().clonesDestroy( agentSet );
            productOperation.addLog( "Lxc containers successfully destroyed" );
        }
        catch ( LxcDestroyException ex )
        {
            productOperation.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
        }

        productOperation.addLog( "Deleting cluster information from database.." );

        manager.getPluginDAO().deleteInfo( CassandraClusterConfig.PRODUCT_KEY, config.getClusterName() );
        productOperation.addLogDone( "Cluster info deleted from database" );
    }
}
