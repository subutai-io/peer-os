package org.safehaus.subutai.plugin.lucene.impl;

import java.util.Iterator;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.lucene.api.Config;


class OverHadoopSetupStrategy extends LuceneSetupStrategy {

    public OverHadoopSetupStrategy(LuceneImpl manager, Config config, ProductOperation po) {
        super(manager, config, po);
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
                po.addLog(
                    String.format( "Node %s is not connected. Omitting this node from installation",
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
            throw new ClusterSetupException( "Failed to check presence of installed subutai packages\nInstallation aborted" );
        }

        for ( Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); )
        {
            Agent node = it.next();
            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );

            if ( result.getStdOut().contains( "ksks-lucene" ) )
            {
                po.addLog(
                    String.format( "Node %s already has Lucene installed. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
            else if ( !result.getStdOut().contains( "ksks-hadoop" ) )
            {
                po.addLog(
                    String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }

        // Save to db
        po.addLog( "Installing Lucene..." );

        Command installCommand = manager.getCommands().getInstallCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() )
        {
            po.addLog( "Installation succeeded\nUpdating db..." );

            try
            {
                manager.getDbManager().saveInfo2( Config.PRODUCT_KEY, config.getClusterName(), config );

                po.addLogDone( "Information updated in db" );
            }
            catch ( DBException e )
            {
                throw new ClusterSetupException( String.format( "Failed to update information in db, %s", e.getMessage() ) );
            }
        }
        else
        {
            throw new ClusterSetupException( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
        }

        return config;
    }

}
