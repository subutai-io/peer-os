package org.safehaus.kiskis.mgmt.impl.solr.handler;


import com.google.common.base.Strings;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class InstallOperationHandler extends AbstractOperationHandler<SolrImpl> {
    private final Config config;


    public InstallOperationHandler( SolrImpl manager, Config config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        productOperation = manager.getTracker()
                    .createProductOperation( Config.PRODUCT_KEY, String.format( "Installing %s", Config.PRODUCT_KEY ) );
    }


    @Override
    public void run() {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) || config.getNumberOfNodes() <= 0 ) {
            productOperation.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( manager.getCluster( config.getClusterName() ) != null ) {
            productOperation.addLogFailed(
                    String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                            config.getClusterName() ) );
            return;
        }

        try {
            productOperation.addLog( String.format( "Creating %d lxc containers...", config.getNumberOfNodes() ) );
            Map<Agent, Set<Agent>> lxcAgentsMap = manager.getLxcManager().createLxcs( config.getNumberOfNodes() );
            config.setNodes( new HashSet<Agent>() );

            for ( Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet() ) {
                config.getNodes().addAll( entry.getValue() );
            }

            productOperation.addLog( "Lxc containers created successfully\nUpdating db..." );

            if ( manager.getDbManager().saveInfo( Config.PRODUCT_KEY, config.getClusterName(), config ) ) {
                productOperation.addLog( "Cluster info saved to DB\nInstalling Solr..." );
                Command installCommand = manager.getCommands().getInstallCommand( config.getNodes() );
                manager.getCommandRunner().runCommand( installCommand );

                if ( installCommand.hasSucceeded() ) {
                    productOperation.addLogDone( "Installation succeeded" );
                }
                else {
                    productOperation.addLogFailed(
                            String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
                }
            }
            else {
                // Destroy all LXCs also
                try {
                    manager.getLxcManager().destroyLxcs( lxcAgentsMap );
                }
                catch ( LxcDestroyException ex ) {
                    productOperation.addLogFailed(
                            "Could not save cluster info to DB! Please see logs. Use LXC module to "
                                    + "cleanup\nInstallation aborted" );
                }
                productOperation.addLogFailed(
                        "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
            }
        }
        catch ( LxcCreateException ex ) {
            productOperation.addLogFailed( ex.getMessage() );
        }
    }
}
