package org.safehaus.subutai.plugin.shark.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.Commands;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import com.google.common.collect.Sets;


public class AddNodeOperationHandler extends AbstractOperationHandler<SharkImpl>
{
    private final String lxcHostname;


    public AddNodeOperationHandler( SharkImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( SharkClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }


    @Override
    public void run()
    {
        SharkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        //check if node agent is connected
        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            productOperation
                    .addLogFailed( String.format( "Node %s is not connected. Operation aborted", lxcHostname ) );
            return;
        }

        if ( config.getNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Node %s already belongs to this cluster. Operation aborted", lxcHostname ) );
            return;
        }

        SparkClusterConfig sparkConfig = manager.getSparkManager().getCluster( clusterName );
        if ( sparkConfig == null )
        {
            productOperation
                    .addLogFailed( String.format( "Spark cluster '%s' not found. Installation aborted", clusterName ) );
            return;
        }

        if ( !sparkConfig.getAllNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Node %s does not belong to %s spark cluster. Operation aborted", lxcHostname,
                            clusterName ) );
            return;
        }

        productOperation.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            productOperation
                    .addLogFailed( "Failed to check presence of installed ksks packages. Installation aborted" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( agent.getUuid() );

        if ( result.getStdOut().contains( "ksks-shark" ) )
        {
            productOperation.addLogFailed(
                    String.format( "Node %s already has Shark installed. Installation aborted", lxcHostname ) );
            return;
        }
        else if ( !result.getStdOut().contains( "ksks-spark" ) )
        {
            productOperation.addLogFailed(
                    String.format( "Node %s has no Spark installation. Installation aborted", lxcHostname ) );
            return;
        }

        config.getNodes().add( agent );
        productOperation.addLog( "Updating db..." );
        //save to db
        if ( manager.getDbManager().saveInfo( SharkClusterConfig.PRODUCT_KEY, config.getClusterName(), config ) )
        {
            productOperation.addLog( "Cluster info updated in DB. Installing Shark..." );

            Command installCommand = Commands.getInstallCommand( Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() )
            {
                productOperation.addLog( "Installation succeeded. Setting Master IP..." );

                Command setMasterIPCommand =
                        Commands.getSetMasterIPCommand( Sets.newHashSet( agent ), sparkConfig.getMasterNode() );
                manager.getCommandRunner().runCommand( setMasterIPCommand );

                if ( setMasterIPCommand.hasSucceeded() )
                {
                    productOperation.addLogDone( "Master IP set successfully. Done" );
                }
                else
                {
                    productOperation.addLogFailed(
                            String.format( "Failed to set Master IP, %s", setMasterIPCommand.getAllErrors() ) );
                }
            }
            else
            {

                productOperation
                        .addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
            }
        }
        else
        {
            productOperation
                    .addLogFailed( "Could not update cluster info in DB! Please see logs. Installation aborted" );
        }
    }
}
