package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

import com.google.common.collect.Sets;


public class AddNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{
    private final String lxcHostname;


    public AddNodeOperationHandler( ElasticsearchImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        trackerOperation = manager.getTracker().createTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    @Override
    public void run()
    {
        ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = manager.getCluster( clusterName );
        if ( elasticsearchClusterConfiguration == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        //check if node agent is connected
        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            trackerOperation
                    .addLogFailed( String.format( "Node %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( elasticsearchClusterConfiguration.getNodes().contains( agent ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        trackerOperation.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Command checkInstalledCommand = manager.getCommands().getCheckInstalledCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            trackerOperation
                    .addLogFailed( "Failed to check presence of installed ksks packages\nInstallation aborted" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( agent.getUuid() );

        if ( result.getStdOut().contains( "ksks-elasticsearch" ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Node %s already has Elasticsearch installed\nInstallation aborted", lxcHostname ) );
            return;
        }

        elasticsearchClusterConfiguration.getNodes().add( agent );
        trackerOperation.addLog( "Updating db..." );
        //save to db
        if ( manager.getDbManager().saveInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                elasticsearchClusterConfiguration.getClusterName(), elasticsearchClusterConfiguration ) )
        {
            trackerOperation.addLog( "Cluster info updated in DB\nInstalling Mahout..." );
            //install mahout

            Command installCommand = manager.getCommands().getInstallCommand( Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() )
            {
                trackerOperation.addLogDone( "Installation succeeded\nDone" );
            }
            else
            {

                trackerOperation
                        .addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
            }
        }
        else
        {
            trackerOperation
                    .addLogFailed( "Could not update cluster info in DB! Please see logs\nInstallation aborted" );
        }
    }
}
