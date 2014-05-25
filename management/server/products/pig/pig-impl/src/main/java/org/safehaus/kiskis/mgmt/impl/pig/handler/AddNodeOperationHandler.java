package org.safehaus.kiskis.mgmt.impl.pig.handler;


import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.pig.Config;
import org.safehaus.kiskis.mgmt.impl.pig.PigImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;


public class AddNodeOperationHandler extends AbstractOperationHandler<PigImpl> {
    private final String lxcHostname;


    public AddNodeOperationHandler( PigImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    @Override
    public void run() {
        Config config = manager.getCluster( clusterName );
        if ( config == null ) {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        // Check if node agent is connected
        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null ) {
            productOperation
                    .addLogFailed( String.format( "Node %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( config.getNodes().contains( agent ) ) {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        productOperation.addLog( "Checking prerequisites..." );

        // Check installed ksks packages
        Command checkInstalledCommand = manager.getCommands().getCheckInstalledCommand( Util.wrapAgentToSet( agent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            productOperation
                    .addLogFailed( "Failed to check presence of installed ksks packages\nInstallation aborted" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( agent.getUuid() );

        if ( result.getStdOut().contains( "ksks-pig" ) ) {
            productOperation.addLogFailed(
                    String.format( "Node %s already has Pig installed\nInstallation aborted", lxcHostname ) );
            return;
        }
        else if ( !result.getStdOut().contains( "ksks-hadoop" ) ) {
            productOperation.addLogFailed(
                    String.format( "Node %s has no Hadoop installation\nInstallation aborted", lxcHostname ) );
            return;
        }

        config.getNodes().add( agent );
        productOperation.addLog( "Updating db..." );

        // Save to db
        if ( manager.getDbManager().saveInfo( Config.PRODUCT_KEY, config.getClusterName(), config ) ) {

            productOperation.addLog( "Cluster info updated in DB\nInstalling Pig..." );
            Command installCommand = manager.getCommands().getInstallCommand( Util.wrapAgentToSet( agent ) );
            manager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() ) {
                productOperation.addLogDone( "Installation succeeded\nDone" );
            }
            else {
                productOperation
                        .addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
            }
        }
        else {
            productOperation
                    .addLogFailed( "Could not update cluster info in DB! Please see logs\nInstallation aborted" );
        }
    }
}
