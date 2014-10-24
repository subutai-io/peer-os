package org.safehaus.subutai.core.peer.cli;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerInterface;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RemotePeer;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "peer", name = "remote-clone")
public class RemoteCloneCommand extends OsgiCommandSupport
{

    private PeerManager peerManager;


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Argument(index = 0, name = "peerId", multiValued = false, description = "Remote peer UUID")
    private String peerId;


    @Argument(index = 1, name = "envId", multiValued = false, description = "Environment UUID")
    private String envId;

    @Argument(index = 2, name = "templateName", multiValued = false, description = "Remote template name")
    private String templateName;

    @Argument(index = 3, name = "quantity", multiValued = false, description = "Number of containers to clone")
    private int quantity;

    @Argument(index = 4, name = "strategyId", multiValued = false, description = "Container placement strategy")
    private String strategyId;


    @Override
    protected Object doExecute() throws Exception
    {

        PeerInterface peer = peerManager.getPeer( UUID.fromString( peerId ) );
        if ( peer == null || !( peer instanceof RemotePeer ) )
        {
            System.out.println( "Could not get RemotePeer interface." );
            return null;
        }

        UUID environmentId = UUID.fromString( envId );
        Set<ContainerHost> containers =
                peer.createContainers( peerManager.getSiteId(), environmentId, templateName, quantity, strategyId,
                        null );

        System.out.println(
                String.format( "Containers successfully created.\nList of new %d containers:\n", containers.size() ) );
        System.out.println( String.format( "Hostname\tParent hostname\tAgent ID\tEnv ID" ) );
        for ( ContainerHost containerHost : containers )
        {
            System.out.println(
                    String.format( "%s\t%s\t%s\t%s", containerHost.getHostname(), containerHost.getParentHostname(),
                            containerHost.getId(), containerHost.getEnvironmentId() ) );
        }
        return null;
    }
}
