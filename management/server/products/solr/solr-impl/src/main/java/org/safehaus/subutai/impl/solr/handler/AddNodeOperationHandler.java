package org.safehaus.subutai.impl.solr.handler;


import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.impl.solr.SolrImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;


public class AddNodeOperationHandler extends AbstractOperationHandler<SolrImpl> {

    public AddNodeOperationHandler( SolrImpl manager, String clusterName ) {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    @Override
    public void run() {
        Config config = manager.getCluster( clusterName );

        if ( config == null ) {
            productOperation.addLogFailed(
                    String.format( "Installation with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        try {
            productOperation.addLog( "Creating lxc container..." );

            Map<Agent, Set<Agent>> lxcAgentsMap = manager.getLxcManager().createLxcs( 1 );

            Agent lxcAgent = lxcAgentsMap.entrySet().iterator().next().getValue().iterator().next();

            config.getNodes().add( lxcAgent );
            productOperation.addLog( "Lxc container created successfully\nUpdating db..." );

            if ( manager.getDbManager().saveInfo( Config.PRODUCT_KEY, clusterName, config ) ) {
                productOperation.addLog( "Cluster info updated in DB\nInstalling Solr..." );

                Command installCommand = manager.getCommands().getInstallCommand( Util.wrapAgentToSet( lxcAgent ) );
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
                productOperation.addLogFailed(
                        "Error while updating installation info in DB. Check logs. Use LXC Module to cleanup\nFailed" );
            }
        }
        catch ( LxcCreateException ex ) {
            productOperation.addLogFailed( ex.getMessage() );
        }
    }
}
