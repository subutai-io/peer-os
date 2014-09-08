package org.safehaus.subutai.plugin.elasticsearch.impl;


import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.elasticsearch.api.Config;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;

import com.google.common.collect.Sets;


public class AddNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{
    private final String lxcHostname;


    public AddNodeOperationHandler( ElasticsearchImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
            String.format( "Adding node to %s", clusterName ) );
    }


    @Override
    public void run()
    {
        Config config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        //check if node agent is connected
        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            productOperation.addLogFailed(
                    String.format( "Node %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( config.getNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        productOperation.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            productOperation.addLogFailed(
                    "Failed to check presence of installed ksks packages\nInstallation aborted" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( agent.getUuid() );

        if ( result.getStdOut().contains( "ksks-elasticsearch" ) )
        {
            productOperation.addLogFailed(
                    String.format( "Node %s already has Elasticsearch installed\nInstallation aborted", lxcHostname ) );
            return;
        }

        config.getNodes().add( agent );
        productOperation.addLog( "Updating db..." );
        //save to db
        if ( manager.getDbManager().saveInfo( Config.PRODUCT_KEY, config.getClusterName(), config ) )
        {
            productOperation.addLog( "Cluster info updated in DB\nInstalling Mahout..." );
            //install mahout

            Command installCommand = Commands.getInstallCommand( Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() )
            {
                productOperation.addLogDone( "Installation succeeded\nDone" );
            }
            else
            {

                productOperation.addLogFailed(
                        String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
            }
        }
        else
        {
            productOperation.addLogFailed(
                    "Could not update cluster info in DB! Please see logs\nInstallation aborted" );
        }
    }
}
