package org.safehaus.subutai.plugin.oozie.impl.handler;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;


public class DestroyNodeOperationHandler extends AbstractOperationHandler<OozieImpl>
{

    private String lxcHostName;
    private final ProductOperation productOperation;


    public DestroyNodeOperationHandler( OozieImpl manager, String clusterName, String lxcHostName )
    {
        super( manager, clusterName );
        this.lxcHostName = lxcHostName;
        productOperation = manager.getTracker().createProductOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Uninstalling oozie client from %s node and updating cluster information of %s", lxcHostName,
                        clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }

    @Override
    public void run()
    {
        OozieClusterConfig oozieClusterConfig = manager.getCluster( clusterName );
        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostName );

        if ( oozieClusterConfig == null )
        {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        if ( node == null )
        {
            productOperation.addLogFailed( "Node is not connected" );
            return;
        }

        if ( !oozieClusterConfig.getClients().contains( node ) )
        {
            productOperation
                    .addLogFailed( String.format( "Node in %s cluster as a client does not exist", clusterName ) );
            return;
        }


        Set<Agent> clientNodesToBeRemoved = new HashSet<>();
        clientNodesToBeRemoved.add( node );
        Command removeClientCommand = manager.getCommands().getUninstallClientsCommand( clientNodesToBeRemoved );
        manager.getCommandRunner().runCommand( removeClientCommand );
        productOperation.addLog( removeClientCommand.getDescription() );


        oozieClusterConfig.removeClient( node );

        manager.getPluginDAO()
               .saveInfo( OozieClusterConfig.PRODUCT_KEY, oozieClusterConfig.getClusterName(), oozieClusterConfig );
        productOperation.addLogDone( "Cluster info saved to DB" );
    }

}

