package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "reserve-net-resource" )
public class ReserveNetworkResourceCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;

    @Argument( index = 0, name = "env-id", multiValued = false, required = true, description = "Env ID" )
    private String envId;

    @Argument( index = 1, name = "vni", multiValued = false, required = true, description = "VNI" )
    private long vni;

    @Argument( index = 2, name = "p2p-subnet", multiValued = false, required = true, description = "P2P subnet" )
    private String p2pSubnet;

    @Argument( index = 3, name = "container-subnet", multiValued = false, required = true, description = "Container "
            + "subnet" )
    private String containerSubnet;


    public ReserveNetworkResourceCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        try
        {
            localPeer.reserveNetworkResource( envId, vni, p2pSubnet, containerSubnet );

            System.out.println( "Network resource reserved" );
        }
        catch ( PeerException e )
        {
            System.out.println( e.getMessage() );
        }
        return null;
    }
}
