package org.safehaus.subutai.plugin.oozie.impl.handler;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;


public class UninstallHandler extends AbstractOperationHandler<OozieImpl, OozieClusterConfig>
{

    private final TrackerOperation trackerOperation;
    //    private OozieConfig config;
    private String clusterName;


    public UninstallHandler( final OozieImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        trackerOperation = manager.getTracker().createTrackerOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Unistalling %s cluster...", clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        /*manager.getExecutor().execute( new Runnable()
        {

            public void run()
            {
                OozieClusterConfig config = manager.getPluginDAO().getInfo( OozieClusterConfig.PRODUCT_KEY, clusterName,
                        OozieClusterConfig.class );
                if ( config == null )
                {
                    trackerOperation.addLogFailed(
                            String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
                    return;
                }

                Set<String> nodes = new HashSet<String>();
                for ( Agent agent : config.getAllOozieAgents() )
                {
                    nodes.add( agent.getHostname() );
                }

                for ( String node : nodes )
                {
                    if ( manager.getAgentManager().getAgentByHostname( node ) == null )
                    {
                        trackerOperation.addLogFailed( String.format( "Node %s not connected. Aborted", node ) );
                        return;
                    }
                }

                Set<Agent> servers = new HashSet<Agent>();
                Agent serverAgent = config.getServer();
                servers.add( serverAgent );

                Command uninstallServerCommand = manager.getCommands().getUninstallServerCommand( servers );
                manager.getCommandRunner().runCommand( uninstallServerCommand );

                if ( uninstallServerCommand.hasSucceeded() )
                {
                    trackerOperation.addLog( "Uninstall server succeeded" );
                }
                else
                {
                    trackerOperation.addLogFailed(
                            String.format( "Uninstall server failed, %s", uninstallServerCommand.getAllErrors() ) );
                    return;
                }

                Set<Agent> clientAgents = new HashSet<Agent>();
                for ( Agent clientNode : config.getClients() )
                {
                    Agent clientAgent = clientNode;
                    clientAgents.add( clientAgent );
                }
                if ( !clientAgents.isEmpty() )
                {
                    Command uninstallClientsCommand = manager.getCommands().getUninstallClientsCommand( clientAgents );
                    manager.getCommandRunner().runCommand( uninstallClientsCommand );

                    if ( uninstallClientsCommand.hasSucceeded() )
                    {
                        trackerOperation.addLog( "Uninstall clients succeeded" );
                    }
                    else
                    {
                        trackerOperation.addLogFailed( String.format( "Uninstall clients failed, %s",
                                uninstallClientsCommand.getAllErrors() ) );
                        return;
                    }
                }


                trackerOperation.addLog( "Updating db..." );
                manager.getPluginDAO().deleteInfo( OozieClusterConfig.PRODUCT_KEY, config.getClusterName() );
                // TODO check if delete is succesful
                trackerOperation.addLogDone( "Cluster info deleted from DB\nDone" );
            }
        } );*/
    }
}
