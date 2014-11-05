package org.safehaus.subutai.plugin.elasticsearch.impl;


import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;


public class ClusterConfiguration
{

    private ElasticsearchImpl manager;
    private TrackerOperation po;


    public ClusterConfiguration( final ElasticsearchImpl manager, final TrackerOperation po )
    {
        this.manager = manager;
        this.po = po;
    }


    public void configureCluster( final ElasticsearchClusterConfiguration config, Environment environment )
            throws ClusterConfigurationException
    {
        // es-conf.sh cluster_name test
        String clusterConfigureCommand = Commands.configure + "cluster_name " + config.getClusterName();

        for ( ContainerHost containerHost : environment.getContainers() )
        {
            try
            {
                po.addLog( "Configuring node: " + containerHost.getId() );
                // Setting cluster name
                CommandResult commandResult = containerHost.execute( new RequestBuilder( clusterConfigureCommand ) );
                po.addLog( commandResult.getStdOut() );
            }
            catch ( CommandException e )
            {
                po.addLogFailed( String.format( "Installation failed" ) );
                throw new ClusterConfigurationException( e.getMessage() );
            }
        }
        config.setEnvironmentId( environment.getId() );
        manager.getPluginDAO()
               .saveInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY, config.getClusterName(), config );
        po.addLogDone( ElasticsearchClusterConfiguration.PRODUCT_KEY + " cluster data saved into database" );
    }
}
