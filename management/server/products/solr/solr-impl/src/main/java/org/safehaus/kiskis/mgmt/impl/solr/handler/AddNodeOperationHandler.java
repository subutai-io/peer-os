package org.safehaus.kiskis.mgmt.impl.solr.handler;


import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.Map;
import java.util.Set;
import java.util.UUID;


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
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
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
                        "Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed" );
            }
        }
        catch ( LxcCreateException ex ) {
            productOperation.addLogFailed( ex.getMessage() );
        }
    }
}
