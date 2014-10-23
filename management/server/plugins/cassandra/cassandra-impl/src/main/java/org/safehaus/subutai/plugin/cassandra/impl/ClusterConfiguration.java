package org.safehaus.subutai.plugin.cassandra.impl;


import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.AgentUtil;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;


public class ClusterConfiguration
{

    private static final Logger LOG = Logger.getLogger( ClusterConfiguration.class.getName() );
    private TrackerOperation po;
    private CassandraImpl cassandraManager;


    public ClusterConfiguration( final TrackerOperation trackerOperation, final CassandraImpl cassandraManager )
    {
        this.po = trackerOperation;
        this.cassandraManager = cassandraManager;
    }


    public void configureCluster( CassandraClusterConfig config ) throws ClusterConfigurationException
    {

        // setting cluster name
        po.addLog( "Setting cluster name " + config.getClusterName() );
        Set<Agent> agentSet = cassandraManager.getAgentManager().returnAgentsByGivenUUIDSet( config.getNodes() );
        Command setClusterNameCommand = cassandraManager.getCommands().getConfigureCommand( agentSet,
                "cluster_name " + config.getClusterName() );
        cassandraManager.getCommandRunner().runCommand( setClusterNameCommand );

        if ( setClusterNameCommand.hasSucceeded() )
        {
            po.addLog( "Configure cluster name succeeded" );
        }
        else
        {
            po.addLogFailed( String.format( "Installation failed, %s", setClusterNameCommand.getAllErrors() ) );
            return;
        }

        // setting data directory name
        po.addLog( "Setting data directory: " + config.getDataDirectory() );
        Command setDataDirCommand = cassandraManager.getCommands().getConfigureCommand( agentSet,
                "data_dir " + config.getDataDirectory() );
        cassandraManager.getCommandRunner().runCommand( setDataDirCommand );

        if ( setDataDirCommand.hasSucceeded() )
        {
            po.addLog( "Configure data directory succeeded" );
        }
        else
        {
            po.addLogFailed( String.format( "Installation failed, %s", setDataDirCommand.getAllErrors() ) );
            return;
        }

        // setting commit log directory
        po.addLog( "Setting commit directory: " + config.getCommitLogDirectory() );
        Command setCommitDirCommand = cassandraManager.getCommands().getConfigureCommand( agentSet,
                "commitlog_dir " + config.getCommitLogDirectory() );
        cassandraManager.getCommandRunner().runCommand( setCommitDirCommand );

        if ( setCommitDirCommand.hasSucceeded() )
        {
            po.addLog( "Configure commit directory succeeded" );
        }
        else
        {
            po.addLogFailed( String.format( "Installation failed, %s", setCommitDirCommand.getAllErrors() ) );
            return;
        }

        // setting saved cache directory
        po.addLog( "Setting saved cache directory: " + config.getSavedCachesDirectory() );
        Command setSavedCacheDirCommand = cassandraManager.getCommands().getConfigureCommand( agentSet,
                "saved_cache_dir " + config.getSavedCachesDirectory() );
        cassandraManager.getCommandRunner().runCommand( setSavedCacheDirCommand );

        if ( setSavedCacheDirCommand.hasSucceeded() )
        {
            po.addLog( "Configure saved cache directory succeeded" );
        }
        else
        {
            po.addLogFailed( String.format( "Installation failed, %s", setSavedCacheDirCommand.getAllErrors() ) );
            return;
        }

        // setting rpc address
        po.addLog( "Setting rpc address" );
        Command setRpcAddressCommand = cassandraManager.getCommands()
                                                       .getConfigureRpcAndListenAddressesCommand( agentSet,
                                                               "rpc_address" );
        cassandraManager.getCommandRunner().runCommand( setRpcAddressCommand );

        if ( setRpcAddressCommand.hasSucceeded() )
        {
            po.addLog( "Configure rpc address succeeded" );
        }
        else
        {
            po.addLogFailed( String.format( "Installation failed, %s", setRpcAddressCommand.getAllErrors() ) );
            return;
        }

        // setting listen address
        po.addLog( "Setting listen address" );
        Command setListenAddressCommand = cassandraManager.getCommands()
                                                          .getConfigureRpcAndListenAddressesCommand( agentSet,
                                                                  "listen_address" );
        cassandraManager.getCommandRunner().runCommand( setListenAddressCommand );

        if ( setListenAddressCommand.hasSucceeded() )
        {
            po.addLog( "Configure listen address succeeded" );
        }
        else
        {
            po.addLogFailed( String.format( "Installation failed, %s", setListenAddressCommand.getAllErrors() ) );
            return;
        }

        // setting seeds
        StringBuilder sb = new StringBuilder();
        //                        sb.append('"');
        for ( UUID seedUUID : config.getSeedNodes() )
        {
            Agent seed = cassandraManager.getAgentManager().getAgentByUUID( seedUUID );
            sb.append( AgentUtil.getAgentIpByMask( seed, Common.IP_MASK ) ).append( "," );
        }
        sb.replace( sb.toString().length() - 1, sb.toString().length(), "" );
        //                        sb.append('"');
        po.addLog( "Settings seeds " + sb.toString() );

        Command setSeedsCommand =
                cassandraManager.getCommands().getConfigureCommand( agentSet, "seeds " + sb.toString() );
        cassandraManager.getCommandRunner().runCommand( setSeedsCommand );

        if ( setSeedsCommand.hasSucceeded() )
        {
            po.addLog( "Configure seeds succeeded" );
        }
        else
        {
            po.addLogFailed( String.format( "Installation failed, %s", setSeedsCommand.getAllErrors() ) );
            return;
        }

        cassandraManager.getPluginDAO().saveInfo( CassandraClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLog( "Cassandra cluster data saved into database" );
    }
}
