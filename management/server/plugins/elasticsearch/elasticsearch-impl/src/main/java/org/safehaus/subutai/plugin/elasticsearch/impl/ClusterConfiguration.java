package org.safehaus.subutai.plugin.elasticsearch.impl;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.common.exception.*;
import org.safehaus.subutai.common.tracker.*;


public class ClusterConfiguration {

    private ElasticsearchImpl manager;
    private ProductOperation po;


    public ClusterConfiguration( final ElasticsearchImpl manager, final ProductOperation po ) {
        this.manager = manager;
        this.po = po;
    }


    public void configureCluster( final ElasticsearchClusterConfiguration elasticsearchClusterConfiguration ) throws ClusterConfigurationException {

        // Setting cluster name

        po.addLog( "Setting cluster name: " + elasticsearchClusterConfiguration.getClusterName() );

        Command setClusterNameCommand =
                Commands.getConfigureCommand( elasticsearchClusterConfiguration.getNodes(), "cluster.name " + elasticsearchClusterConfiguration.getClusterName() );
        manager.getCommandRunner().runCommand( setClusterNameCommand );

        if ( setClusterNameCommand.hasSucceeded() ) {
            po.addLog( "Configure cluster name succeeded" );
        }
        else {
            throw new ClusterConfigurationException( String.format( "Installation failed, %s", setClusterNameCommand.getAllErrors() ) );
        }

        // Setting master nodes

        po.addLog( "Setting master nodes..." );

        Command setMasterNodesCommand = Commands.getConfigureCommand( elasticsearchClusterConfiguration.getMasterNodes(), "node.master true" );
        manager.getCommandRunner().runCommand( setMasterNodesCommand );

        if ( setMasterNodesCommand.hasSucceeded() ) {
            po.addLog( "Master nodes setup successful" );
        }
        else {
            throw new ClusterConfigurationException( String.format( "Installation failed, %s", setMasterNodesCommand.getAllErrors() ) );
        }

        // Setting data nodes

        po.addLog( "Setting data nodes..." );

        Command dataNodesCommand = Commands.getConfigureCommand( elasticsearchClusterConfiguration.getDataNodes(), "node.data true" );
        manager.getCommandRunner().runCommand( dataNodesCommand );

        if ( dataNodesCommand.hasSucceeded() ) {
            po.addLog( "Data nodes setup successful" );
        }
        else {
            throw new ClusterConfigurationException( String.format( "Installation failed, %s", dataNodesCommand.getAllErrors() ) );
        }

        // Setting number of shards

        po.addLog( "Setting number of shards..." );

        Command shardsCommand = Commands.getConfigureCommand( elasticsearchClusterConfiguration.getNodes(),
                "index.number_of_shards " + elasticsearchClusterConfiguration.getNumberOfShards() );
        manager.getCommandRunner().runCommand( shardsCommand );

        if ( !shardsCommand.hasSucceeded() ) {
            throw new ClusterConfigurationException( String.format( "Installation failed, %s", shardsCommand.getAllErrors() ) );
        }

        // Setting number of replicas

        po.addLog( "Setting number of replicas..." );

        Command numberOfReplicasCommand = Commands.getConfigureCommand( elasticsearchClusterConfiguration.getNodes(),
                "index.number_of_replicas " + elasticsearchClusterConfiguration.getNumberOfReplicas() );
        manager.getCommandRunner().runCommand( numberOfReplicasCommand );

        if ( !numberOfReplicasCommand.hasSucceeded() ) {
            throw new ClusterConfigurationException( String.format( "Installation failed, %s", numberOfReplicasCommand.getAllErrors() ) );
        }

        po.addLogDone( "Installation of Elasticsearch cluster succeeded" );
    }

}
