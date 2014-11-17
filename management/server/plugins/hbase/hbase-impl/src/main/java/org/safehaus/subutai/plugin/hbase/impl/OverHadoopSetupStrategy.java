package org.safehaus.subutai.plugin.hbase.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;


public class OverHadoopSetupStrategy extends SetupBase implements ClusterSetupStrategy
{

    public OverHadoopSetupStrategy( HBaseImpl manager, TrackerOperation po, HBaseClusterConfig config )
    {
        super( po, manager, config );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        check();
        configure();
        return config;
    }


    private void check() throws ClusterSetupException
    {

        String m = "Malformed configuration: ";
        if ( config.getClusterName() == null || config.getClusterName().isEmpty() )
        {
            throw new ClusterSetupException( m + "cluster name not specified" );
        }
        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    m + String.format( "cluster %s already exists", config.getClusterName() ) );
        }

        if ( config.getHbaseMaster() == null )
        {
            throw new ClusterSetupException( m + "master node not specified" );
        }

        if ( config.getRegionServers().isEmpty() )
        {
            throw new ClusterSetupException( m + "no region server nodes" );
        }

        if ( config.getQuorumPeers().isEmpty() )
        {
            throw new ClusterSetupException( m + "no quorum nodes" );
        }

        if ( config.getBackupMasters().isEmpty() )
        {
            throw new ClusterSetupException( m + "no backup master nodes" );
        }

        // check if nodes are connected
      /*  if ( manager.getAgentManager().getAgentByHostname( config.getHbaseMaster().getHostname() ) == null )
        {
            throw new ClusterSetupException( "Master node is not connected" );
        }

        for ( UUID a : config.getRegionServers() )
        {
            if ( manager.getAgentManager().getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Not all region server are connected" );
            }
        }

        for ( UUID a : config.getQuorumPeers() )
        {
            if ( manager.getAgentManager().getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Not all quorum peer nodes are connected" );
            }
        }

        for ( UUID a : config.getBackupMasters() )
        {
            if ( manager.getAgentManager().getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Not all backup master nodes are connected" );
            }
        }*/

        // check Hadoop cluster
        HadoopClusterConfig hc = manager.getHadoopManager().getCluster( config.getHadoopClusterName() );
        if ( hc == null )
        {
            throw new ClusterSetupException( "Could not find Hadoop cluster " + config.getHadoopClusterName() );
        }
        if ( !hc.getAllNodes().containsAll( config.getAllNodes() ) )
        {
            throw new ClusterSetupException(
                    "Not all nodes belong to Hadoop cluster " + config.getHadoopClusterName() );
        }
        config.setHadoopNodes( new HashSet<>( hc.getAllNodes() ) );

        po.addLog( "Checking prerequisites..." );

        //check installed subutai packages
        /*Set<UUID> allNodes = config.getAllNodes();
        Command checkInstalledCommand = manager.getCommands().getCheckInstalledCommand( allNodes );*/
        /*manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            throw new ClusterSetupException(
                    "Failed to check presence of installed subutai packages\nInstallation aborted" );
        }
        for ( Iterator<UUID> it = allNodes.iterator(); it.hasNext(); )
        {
            UUID node = it.next();

            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );
            if ( result.getStdOut().contains( Commands.PACKAGE_NAME ) )
            {
                po.addLog( String.format( "Node %s already has HBase installed. Omitting this node from installation",
                        node.getHostname() ) );
                config.getAllNodes().remove( node );
                it.remove();
            }
            else if ( !result.getStdOut()
                             .contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_KEY.toLowerCase() ) )
            {
                po.addLog( String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname() ) );
                config.getAllNodes().remove( node );
                it.remove();
            }
        }

        if ( config.getRegionServers().isEmpty() || config.getQuorumPeers().isEmpty() || config.getBackupMasters()
                                                                                               .isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation\nInstallation aborted" );
        }
        if ( !allNodes.contains( config.getHbaseMaster() ) )
        {
            throw new ClusterSetupException( "Master node was omitted\nInstallation aborted" );
        }*/
    }


    private void configure() throws ClusterSetupException
    {
       /* po.addLog( "Updating db..." );
        //save to db
        manager.getPluginDAO().saveInfo( HBaseClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLog( "Cluster info saved to DB\nInstalling HBase..." );
        //install hbase
        Command installCommand = manager.getCommands().getInstallCommand( config.getAllNodes() );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() )
        {
            po.addLog( "Installation succeeded" );

            SetupHelper helper = new SetupHelper( manager, config, po );
            helper.configureHMaster();
            helper.configureRegionServers();
            helper.configureQuorumPeers();
            helper.configureBackUpMasters();
        }
        else
        {
            throw new ClusterSetupException( "Installation failed: " + installCommand.getAllErrors() );
        }*/
    }
}
