package io.subutai.core.peer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.resource.HostResources;
import io.subutai.bazaar.share.resource.PeerResources;


@Command( scope = "peer", name = "limits", description = "Gets limits for specified peer" )
public class GetResourceLimits extends SubutaiShellCommandSupport
{
    @Argument( name = "peer id", required = true, description = "peer identifier" )
    private String peerId;

    private PeerManager peerManager;


    public GetResourceLimits( PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws PeerException
    {
        Peer peer = peerManager.getPeer( peerId );
        if ( peer == null )
        {
            System.out.println( "Peer not found." );
            return null;
        }
        final PeerResources limits = peer.getResourceLimits( new PeerId( peerManager.getLocalPeer().getId() ) );

        System.out.println(
                String.format( "%s, env:%d, cont:%d, net: %d", limits.getPeerId(), limits.getEnvironmentLimit(),
                        limits.getContainerLimit(), limits.getNetworkLimit() ) );

        for ( HostResources resources : limits.getHostResources() )
        {
            System.out.println( String.format( "\t%s, cpu: %s, ram:%s, disk:%s", resources.getHostId(),
                    resources.getCpuLimit().getPrintValue(), resources.getRamLimit().getPrintValue(),
                    resources.getDiskLimit().getPrintValue() ) );
        }
        return null;
    }
}
