package org.safehaus.subutai.plugin.elasticsearch.impl;

import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.plugin.elasticsearch.api.Config;
import org.safehaus.subutai.common.exception.*;
import org.safehaus.subutai.common.tracker.*;


public class ClusterConfiguration {

    private ElasticsearchImpl manager;
    private ProductOperation po;


    public ClusterConfiguration( final ElasticsearchImpl manager, final ProductOperation po ) {
        this.manager = manager;
        this.po = po;
    }


    public void configureCluster( final Config config ) throws ClusterConfigurationException {

        // Setting cluster name

        po.addLog( "Setting cluster name: " + config.getClusterName() );

        Command setClusterNameCommand =
                Commands.getConfigureCommand( config.getNodes(), "cluster.name " + config.getClusterName() );
        manager.getCommandRunner().runCommand( setClusterNameCommand );

        if ( setClusterNameCommand.hasSucceeded() ) {
            po.addLog( "Configure cluster name succeeded" );
        }
        else {
            throw new ClusterConfigurationException( String.format( "Installation failed, %s", setClusterNameCommand.getAllErrors() ) );
        }

        // Setting master nodes

        po.addLog( "Setting master nodes..." );

        Command setMasterNodesCommand = Commands.getConfigureCommand( config.getMasterNodes(), "node.master true" );
        manager.getCommandRunner().runCommand( setMasterNodesCommand );

        if ( setMasterNodesCommand.hasSucceeded() ) {
            po.addLog( "Master nodes setup successful" );
        }
        else {
            throw new ClusterConfigurationException( String.format( "Installation failed, %s", setMasterNodesCommand.getAllErrors() ) );
        }

        // Setting data nodes

        po.addLog( "Setting data nodes..." );

        Command dataNodesCommand = Commands.getConfigureCommand( config.getDataNodes(), "node.data true" );
        manager.getCommandRunner().runCommand( dataNodesCommand );

        if ( dataNodesCommand.hasSucceeded() ) {
            po.addLog( "Data nodes setup successful" );
        }
        else {
            throw new ClusterConfigurationException( String.format( "Installation failed, %s", dataNodesCommand.getAllErrors() ) );
        }

        // Setting number of shards

        po.addLog( "Setting number of shards..." );

        Command shardsCommand = Commands.getConfigureCommand( config.getNodes(),
                "index.number_of_shards " + config.getNumberOfShards() );
        manager.getCommandRunner().runCommand( shardsCommand );

        if ( !shardsCommand.hasSucceeded() ) {
            throw new ClusterConfigurationException( String.format( "Installation failed, %s", shardsCommand.getAllErrors() ) );
        }

        // Setting number of replicas

        po.addLog( "Setting number of replicas..." );

        Command numberOfReplicasCommand = Commands.getConfigureCommand( config.getNodes(),
                "index.number_of_replicas " + config.getNumberOfReplicas() );
        manager.getCommandRunner().runCommand( numberOfReplicasCommand );

        if ( !numberOfReplicasCommand.hasSucceeded() ) {
            throw new ClusterConfigurationException( String.format( "Installation failed, %s", numberOfReplicasCommand.getAllErrors() ) );
        }

        po.addLogDone( "Installation of Elasticsearch cluster succeeded" );
    }

}
