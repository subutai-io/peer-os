package org.safehaus.subutai.plugin.elasticsearch.impl;


import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
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


    public void configureCluster( final ElasticsearchClusterConfiguration elasticsearchClusterConfiguration )
            throws ClusterConfigurationException
    {

        // Setting cluster name

        po.addLog( "Setting cluster name: " + elasticsearchClusterConfiguration.getClusterName() );

        Command setClusterNameCommand = manager.getCommands()
                                               .getConfigureCommand( elasticsearchClusterConfiguration.getNodes(),
                                                       "cluster.name " + elasticsearchClusterConfiguration
                                                               .getClusterName() );
        manager.getCommandRunner().runCommand( setClusterNameCommand );

        if ( setClusterNameCommand.hasSucceeded() )
        {
            po.addLog( "Configure cluster name succeeded" );
        }
        else
        {
            throw new ClusterConfigurationException(
                    String.format( "Installation failed, %s", setClusterNameCommand.getAllErrors() ) );
        }

        po.addLogDone( "Installation of Elasticsearch cluster succeeded" );
    }
}
