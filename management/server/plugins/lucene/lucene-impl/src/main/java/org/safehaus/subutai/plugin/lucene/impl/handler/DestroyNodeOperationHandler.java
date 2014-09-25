package org.safehaus.subutai.plugin.lucene.impl.handler;


import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.lucene.api.LuceneConfig;
import org.safehaus.subutai.plugin.lucene.impl.Commands;
import org.safehaus.subutai.plugin.lucene.impl.LuceneImpl;

import com.google.common.collect.Sets;


public class DestroyNodeOperationHandler extends AbstractOperationHandler<LuceneImpl>
{
    private final String lxcHostname;


    public DestroyNodeOperationHandler( LuceneImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( LuceneConfig.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        LuceneConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( !config.getNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        if ( config.getNodes().size() == 1 )
        {
            productOperation.addLogFailed(
                    "This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }

        productOperation.addLog( "Uninstalling Lucene..." );
        Command uninstallCommand = Commands.getUninstallCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() )
        {
            AgentResult result = uninstallCommand.getResults().get( agent.getUuid() );
            if ( result.getExitCode() != null && result.getExitCode() == 0 )
            {
                if ( result.getStdOut().contains( "Package ksks-lucene is not installed, so not removed" ) )
                {
                    productOperation.addLog( String.format( "Lucene is not installed, so not removed on node %s",
                            agent.getHostname() ) );
                }
                else
                {
                    productOperation.addLog( String.format( "Lucene is removed from node %s", agent.getHostname() ) );
                }
            }
            else
            {
                productOperation
                        .addLog( String.format( "Error %s on node %s", result.getStdErr(), agent.getHostname() ) );
            }

            config.getNodes().remove( agent );
            productOperation.addLog( "Updating db..." );

            try
            {
                manager.getDbManager().saveInfo2( LuceneConfig.PRODUCT_KEY, clusterName, config );
                productOperation.addLogDone( "Information updated in db" );
            }
            catch ( DBException e )
            {
                productOperation
                        .addLogFailed( String.format( "Failed to update information in db, %s", e.getMessage() ) );
            }
        }
        else
        {
            productOperation.addLogFailed( "Uninstallation failed, command timed out" );
        }
    }
}
