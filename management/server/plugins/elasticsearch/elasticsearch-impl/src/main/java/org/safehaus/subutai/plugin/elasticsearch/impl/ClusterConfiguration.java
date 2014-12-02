package org.safehaus.subutai.plugin.elasticsearch.impl;


import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.ClusterConfigurationInterface;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;


public class ClusterConfiguration implements ClusterConfigurationInterface
{

    private ElasticsearchImpl manager;
    private TrackerOperation po;


    public ClusterConfiguration( final ElasticsearchImpl manager, final TrackerOperation po )
    {
        this.manager = manager;
        this.po = po;
    }


    public void configureCluster( final ConfigBase config, Environment environment ) throws ClusterConfigurationException
    {
        // es-conf.sh cluster_name test
        ElasticsearchClusterConfiguration esConfiguration = ( ElasticsearchClusterConfiguration ) config;
        String clusterConfigureCommand = Commands.configure + "cluster_name " + config.getClusterName();

        for ( ContainerHost containerHost : environment.getContainerHosts() )
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
        esConfiguration.setEnvironmentId( environment.getId() );
        manager.getPluginDAO()
               .saveInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY, config.getClusterName(), config );
        po.addLogDone( ElasticsearchClusterConfiguration.PRODUCT_KEY + " cluster data saved into database" );
    }
}
