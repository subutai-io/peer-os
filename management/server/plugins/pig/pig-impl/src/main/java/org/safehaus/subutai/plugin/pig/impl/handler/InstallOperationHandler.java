package org.safehaus.subutai.plugin.pig.impl.handler;


import com.google.common.base.Strings;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.pig.api.Config;
import org.safehaus.subutai.common.CollectionUtil;
import org.safehaus.subutai.plugin.pig.impl.PigImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.Iterator;


public class InstallOperationHandler extends AbstractOperationHandler<PigImpl>
{
    private final Config config;


    public InstallOperationHandler( PigImpl manager, Config config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        productOperation = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
            String.format( "Installing %s", Config.PRODUCT_KEY ) );
    }


    @Override
    public void run()
    {
        if ( Strings.isNullOrEmpty( config.getHadoopClusterName() ) || CollectionUtil.isCollectionEmpty( config.getNodes() ) )
        {
            productOperation.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            productOperation.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                config.getClusterName() ) );
            return;
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
            productOperation.addLogFailed( "No nodes eligible for installation. Operation aborted" );
            return;
        }

        productOperation.addLog( "Checking prerequisites..." );

        // Check installed packages

        Command checkInstalledCommand = manager.getCommands().getCheckInstalledCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            productOperation.addLogFailed( "Failed to check presence of installed packages\nInstallation aborted" );
            return;
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
            productOperation.addLogFailed( "No nodes eligible for installation. Operation aborted" );
            return;
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
    }
}
