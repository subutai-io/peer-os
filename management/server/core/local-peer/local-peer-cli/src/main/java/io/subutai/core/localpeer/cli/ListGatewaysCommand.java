package io.subutai.core.localpeer.cli;


import java.util.Set;

import org.apache.karaf.shell.commands.Command;

import io.subutai.common.network.Gateway;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "list-gw" )
public class ListGatewaysCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;


    public ListGatewaysCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Set<Gateway> gateways = localPeer.getGateways().list();
        System.out.format( "Found %d gateway(s)%n", gateways.size() );
        for ( Gateway gw : gateways )
        {
            System.out.format( "\t%s\t%d%n", gw.getIp(), gw.getVlan() );
        }
        return null;
    }
}
