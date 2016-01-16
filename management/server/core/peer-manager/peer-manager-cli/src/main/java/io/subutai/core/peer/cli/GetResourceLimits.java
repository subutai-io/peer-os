package io.subutai.core.peer.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.resource.HostResources;
import io.subutai.common.resource.PeerResources;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "limits", description = "Gets limits for specified peer" )
public class GetResourceLimits extends SubutaiShellCommandSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger( GetResourceLimits.class );
    @Argument( index = 0, name = "peer id", required = true, multiValued = false,
            description = "peer identifier" )
    private String peerId;

    private PeerManager peerManager;


    public GetResourceLimits( PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
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
        final PeerResources limits = peer.getResourceLimits( peerId );

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
