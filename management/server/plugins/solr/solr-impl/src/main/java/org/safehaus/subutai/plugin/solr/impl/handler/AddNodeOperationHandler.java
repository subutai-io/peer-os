package org.safehaus.subutai.plugin.solr.impl.handler;


import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.plugin.solr.api.Config;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;
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
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        try {
            productOperation.addLog( "Creating lxc container..." );

            Map<Agent, Set<Agent>> lxcAgentsMap = manager.getLxcManager().createLxcs( 1 );

            Agent lxcAgent = lxcAgentsMap.entrySet().iterator().next().getValue().iterator().next();

            config.getNodes().add( lxcAgent );

            productOperation.addLog( "Lxc container created successfully\nInstalling Solr..." );

            Command installCommand = manager.getCommands().getInstallCommand( Util.wrapAgentToSet( lxcAgent ) );
            manager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() ) {
                productOperation.addLog( "Installation succeeded\nSaving information to database..." );

                try {
                    manager.getDbManager().saveInfo2( Config.PRODUCT_KEY, clusterName, config );
                    productOperation.addLogDone( "Information saved to database" );
                }
                catch ( DBException e ) {
                    productOperation.addLogFailed(
                            String.format( "Failed to save information to database, %s", e.getMessage() ) );
                }
            }
            else {
                productOperation
                        .addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
            }
        }
        catch ( LxcCreateException ex ) {
            productOperation.addLogFailed( ex.getMessage() );
        }
    }
}
