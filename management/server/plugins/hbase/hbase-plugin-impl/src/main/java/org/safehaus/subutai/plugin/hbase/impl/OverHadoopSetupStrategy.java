package org.safehaus.subutai.plugin.hbase.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;


public class OverHadoopSetupStrategy extends HBaseSetupStrategy
{
    public OverHadoopSetupStrategy( HBaseImpl manager, ProductOperation po, HBaseClusterConfig config )
    {
        super( manager, po, config );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        manager.getExecutor().execute( new Runnable()
        {
            public void run()
            {
                if ( manager.getPluginDAO().getInfo( HBaseClusterConfig.PRODUCT_KEY, config.getClusterName(),
                        HBaseClusterConfig.class ) != null )
                {
                    productOperation.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                            config.getClusterName() ) );
                    return;
                }

                Set<Agent> allNodes;
                try
                {
                    allNodes = getAllNodes( config );
                }
                catch ( Exception e )
                {
                    productOperation.addLogFailed( e.getMessage() );
                    return;
                }

                if ( manager.getAgentManager().getAgentByHostname( config.getHadoopNameNode() ) == null )
                {
                    productOperation.addLogFailed( String.format( "Hadoop NameNode %s not connected", config.getHadoopNameNode() ) );
                    return;
                }

                // Installing HBase
                productOperation.addLog( "Installing HBase on ..." );
                for ( Agent agent : allNodes )
                {
                    productOperation.addLog( agent.getHostname() );
                }

                Command installCommand = Commands.getInstallCommand( allNodes );
                manager.getCommandRunner().runCommand( installCommand );

                if ( installCommand.hasSucceeded() )
                {
                    productOperation.addLog( "Installation HBase successful.." );
                }
                else
                {
                    productOperation.addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
                    return;
                }

                productOperation.addLog( "Installation succeeded\nConfiguring master..." );

                // Configuring master
                Command configureMasterCommand = Commands.getConfigMasterTask( allNodes,
                        manager.getAgentManager().getAgentByHostname( config.getHadoopNameNode() ).getHostname(),
                        config.getHbaseMaster().getHostname() );
                manager.getCommandRunner().runCommand( configureMasterCommand );

                if ( configureMasterCommand.hasSucceeded() )
                {
                    productOperation.addLog( "Configure master successful..." );
                }
                else
                {
                    productOperation.addLogFailed( String.format( "Configuration failed, %s", configureMasterCommand ) );
                    return;
                }
                productOperation.addLog( "Configuring master succeeded\nConfiguring region..." );

                // Configuring region
                StringBuilder sbRegion = new StringBuilder();
                for ( Agent agent : config.getRegionServers() )
                {
                    sbRegion.append( agent.getHostname() );
                    sbRegion.append( " " );
                }
                Command configureRegionCommand =
                        Commands.getConfigRegionCommand( allNodes, sbRegion.toString().trim() );
                manager.getCommandRunner().runCommand( configureRegionCommand );

                if ( configureRegionCommand.hasSucceeded() )
                {
                    productOperation.addLog( "Configuring region success..." );
                }
                else
                {
                    productOperation.addLogFailed(
                            String.format( "Configuring failed, %s", configureRegionCommand.getAllErrors() ) );
                    return;
                }
                productOperation.addLog( "Configuring region succeeded\nSetting quorum..." );

                // Configuring quorum
                StringBuilder sbQuorum = new StringBuilder();
                for ( Agent agent : config.getQuorumPeers() )
                {
                    sbQuorum.append( agent.getHostname() );
                    sbQuorum.append( " " );
                }
                Command configureQuorumCommand =
                        Commands.getConfigQuorumCommand( allNodes, sbQuorum.toString().trim() );
                manager.getCommandRunner().runCommand( configureQuorumCommand );

                if ( configureQuorumCommand.hasSucceeded() )
                {
                    productOperation.addLog( "Configuring quorum success..." );
                }
                else
                {
                    productOperation.addLogFailed(
                            String.format( "Installation failed, %s", configureQuorumCommand.getAllErrors() ) );
                    return;
                }
                productOperation.addLog( "Setting quorum succeeded\nSetting backup masters..." );

                // Configuring backup master
                StringBuilder sbBackUpMasters = new StringBuilder();
                for ( Agent agent : config.getBackupMasters() )
                {
                    sbQuorum.append( agent.getHostname() );
                    sbQuorum.append( " " );
                }
                Command configureBackupMasterCommand =
                        Commands.getConfigBackupMastersCommand( allNodes, sbBackUpMasters.toString().trim() );
                manager.getCommandRunner().runCommand( configureBackupMasterCommand );

                if ( configureBackupMasterCommand.hasSucceeded() )
                {
                    productOperation.addLog( "Configuring backup master success..." );
                }
                else
                {
                    productOperation.addLogFailed( String.format( "Installation failed, %s",
                            configureBackupMasterCommand.getAllErrors() ) );
                    return;
                }
                productOperation.addLog( "Cluster installation succeeded\n" );

                manager.getPluginDAO().saveInfo( HBaseClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
                productOperation.addLog( "Cluster info saved to DB\nInstalling HBase..." );
            }
        } );

        return config;
    }


    private Set<Agent> getAllNodes( HBaseClusterConfig config ) throws Exception
    {
        final Set<Agent> allNodes = new HashSet<>();

        if ( config.getHbaseMaster() == null )
        {
            throw new Exception( String.format( "Master node %s not connected", config.getHbaseMaster() ) );
        }
        allNodes.add(  config.getHbaseMaster()  );

        for ( Agent agent : config.getRegionServers() )
        {
            if (  agent  == null )
            {
                throw new Exception( String.format( "Region server node %s not connected", agent ) );
            }
            allNodes.add( agent  );
        }

        for ( Agent agent : config.getQuorumPeers() )
        {
            if (  agent  == null )
            {
                throw new Exception( String.format( "Region server node %s not connected", agent ) );
            }
            allNodes.add( agent  );
        }

        for ( Agent agent : config.getBackupMasters() )
        {
            if (  agent  == null )
            {
                throw new Exception( String.format( "Region server node %s not connected", agent ) );
            }
            allNodes.add( agent  );
        }

        return allNodes;
    }
}
