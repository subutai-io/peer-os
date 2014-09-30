package org.safehaus.subutai.plugin.mahout.impl;



import java.util.Iterator;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.mahout.api.MahoutClusterConfig;


class OverHadoopSetupStrategy extends MahoutSetupStrategy
{

    public OverHadoopSetupStrategy( MahoutImpl manager, MahoutClusterConfig config, ProductOperation po )
    {
        super( manager, config, po );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        checkConfig();

        if ( manager.getHadoopManager().getCluster( config.getHadoopClusterName() ) == null )
        {
            throw new ClusterSetupException( String.format( "Hadoop cluster '%s' not found\nInstallation aborted",
                    config.getHadoopClusterName() ) );
        }

        // Check if node agent is connected
        for ( Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); )
        {
            Agent node = it.next();
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null )
            {
                po.addLog( String.format( "Node %s is not connected. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }

        po.addLog( "Checking prerequisites..." );

        // Check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            throw new ClusterSetupException(
                    "Failed to check presence of installed subutai packages\nInstallation aborted" );
        }

        for ( Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); )
        {
            Agent node = it.next();
            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );

            if ( result.getStdOut().contains( "ksks-mahout" ) )
            {
                po.addLog( String.format( "Node %s already has Mahout installed. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
            else if ( !result.getStdOut().contains( "ksks-hadoop" ) )
            {
                po.addLog( String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }

        // Save to db
        po.addLog( "Installing Mahout..." );

        Command installCommand = Commands.getInstallCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() )
        {
            po.addLog( "Installation succeeded\nUpdating db..." );
            manager.getPluginDAO().saveInfo( MahoutClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            po.addLog( "Cluster information saved into database" );
        }
        else
        {
            throw new ClusterSetupException(
                    String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
        }

        return config;
    }
}
