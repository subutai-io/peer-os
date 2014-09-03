package org.safehaus.subutai.plugin.pig.impl;

import java.util.Iterator;

import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.plugin.pig.api.Config;

import com.google.common.base.Strings;


class OverHadoopSetupStrategy extends PigSetupStrategy {

    public OverHadoopSetupStrategy(PigImpl manager, Config config, ProductOperation po) {
        super(manager, config, po);
    }

    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        if ( Strings.isNullOrEmpty( config.getHadoopClusterName() ) || CollectionUtil
            .isCollectionEmpty( config.getNodes() ) )
        {
            throw new ClusterSetupException("Malformed configuration\nInstallation aborted");
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                            config.getClusterName() ));
        }

        // Check if node agent is connected
        for ( Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); )
        {
            Agent node = it.next();
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null )
            {
                productOperation.addLog(
                    String.format( "Node %s is not connected. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }

        productOperation.addLog( "Checking prerequisites..." );

        // Check installed packages

        Command checkInstalledCommand = manager.getCommands().getCheckInstalledCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            throw new ClusterSetupException( "Failed to check presence of installed packages\nInstallation aborted" );
        }

        for ( Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); )
        {
            Agent node = it.next();
            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );

            if ( result.getStdOut().contains( Config.PRODUCT_PACKAGE ) )
            {
                productOperation.addLog(
                    String.format( "Node %s already has Pig installed. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }

        productOperation.addLog( "Updating db..." );

        // Save to db

        if ( manager.getDbManager().saveInfo( Config.PRODUCT_KEY, config.getClusterName(), config ) )
        {
            productOperation.addLog( "Cluster info saved to DB\nInstalling Pig..." );
            Command installCommand = manager.getCommands().getInstallCommand( config.getNodes() );
            manager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() )
            {
                productOperation.addLogDone( "Installation succeeded\nDone" );
            }
            else
            {
                productOperation
                    .addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
            }
        }
        else
        {
            productOperation.addLogFailed( "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
        }

        return config;
    }

}
