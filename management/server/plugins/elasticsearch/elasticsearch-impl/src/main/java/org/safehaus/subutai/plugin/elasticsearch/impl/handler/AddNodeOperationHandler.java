package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;


public class AddNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{

    private ClusterConfiguration clusterConfiguration;


    public AddNodeOperationHandler( ElasticsearchImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
        clusterConfiguration = new ClusterConfiguration( manager, trackerOperation );
    }


    @Override
    public void run()
    {
        ElasticsearchClusterConfiguration config = manager.getCluster( clusterName );
        try
        {
            //create lxc
            trackerOperation.addLog( "Creating lxc container..." );

            Set<Agent> agents = manager.getContainerManager()
                                       .clone( ElasticsearchClusterConfiguration.getTemplateName(), 1, null,
                                               ElasticsearchClusterConfiguration.getNodePlacementStrategy() );

            Agent agent = agents.iterator().next();

            trackerOperation.addLog( "Lxc container created successfully" );

            config.getNodes().add( agent );
            config.setNumberOfNodes( config.getNumberOfNodes() + 1 );

            //reconfigure cluster
            try
            {
                clusterConfiguration.configureCluster( config );
            }
            catch ( ClusterConfigurationException e )
            {
                trackerOperation.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
                return;
            }

            //update db
            trackerOperation.addLog( "Updating cluster information in database..." );

            manager.getPluginDAO()
                   .saveInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY, config.getClusterName(), config );
            trackerOperation.addLogDone( "Cluster information updated in database" );
        }
        catch ( LxcCreateException ex )
        {
            trackerOperation.addLogFailed( ex.getMessage() );
        }
    }
}
