package org.safehaus.subutai.plugin.shark.impl.handler;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.Commands;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class AddNodeOperationHandler extends AbstractOperationHandler<SharkImpl>
{
    private final String hostname;


    public AddNodeOperationHandler( SharkImpl manager, String clusterName, String hostname )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.productOperation = manager.getTracker().createProductOperation(
                SharkClusterConfig.PRODUCT_KEY, String.format( "Adding node to %s", clusterName ) );
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
        Agent agent = manager.getAgentManager().getAgentByHostname( hostname );
        if ( agent == null )
        {
            productOperation.addLogFailed( String.format( "Node %s is not connected. Operation aborted", hostname ) );
            return;
        }

        if ( config.getNodes().contains( agent ) )
        {
            productOperation.addLogFailed( "Node already belongs to this cluster. Operation aborted" );
            return;
        }

        if ( config.getSparkClusterName() == null )
        {
            productOperation.addLogFailed( "Spark cluster name not specified" );
            return;
        }
        SparkClusterConfig sparkConfig = manager.getSparkManager().getCluster( config.getSparkClusterName() );
        if ( sparkConfig == null )
        {
            productOperation.addLogFailed( String.format( "Underlying Spark cluster '%s' not found.", clusterName ) );
            return;
        }

        if ( !sparkConfig.getAllNodes().contains( agent ) )
        {
            productOperation.addLogFailed( "Node does not belong to Spark cluster. Operation aborted" );
            return;
        }

        productOperation.addLog( "Checking prerequisites..." );

        //check installed packages
        Set<Agent> set = new HashSet<>( Arrays.asList( agent ) );
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( set );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            productOperation.addLogFailed( "Failed to check installed packages. Installation aborted" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( agent.getUuid() );

        if ( result.getStdOut().contains( Commands.PACKAGE_NAME ) )
        {
            productOperation.addLogFailed( "Node already has Shark installed. Installation aborted" );
            return;
        }

        config.getNodes().add( agent );

        productOperation.addLog( "Updating db..." );
        try
        {
            manager.getPluginDao().saveInfo( SharkClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            productOperation.addLog( "Cluster info updated in DB" );
        }
        catch ( Exception ex )
        {
            productOperation.addLogFailed( "Could not update cluster info in DB: " + ex.getMessage() );
            return;
        }

        productOperation.addLog( "Installing Shark..." );

        Command installCommand = Commands.getInstallCommand( set );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() )
        {
            productOperation.addLog( "Installation succeeded. Setting Master IP..." );

            Command cmd = Commands.getSetMasterIPCommand( set, sparkConfig.getMasterNode() );
            manager.getCommandRunner().runCommand( cmd );

            if ( cmd.hasSucceeded() )
            {
                productOperation.addLogDone( "Master IP set successfully. Done" );
            }
            else
            {
                productOperation.addLogFailed( "Failed to set Master IP:" + cmd.getAllErrors() );
            }
        }
        else
        {
            productOperation.addLogFailed( "Installation failed: " + installCommand.getAllErrors() );
        }

    }


}

