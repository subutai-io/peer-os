package io.subutai.core.network.cli;


import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.protocol.ReservedPort;
import io.subutai.common.protocol.ReservedPorts;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "net", name = "reserved-ports", description = "List reserved ports on host" )
public class ListReservedPortsCommand extends SubutaiShellCommandSupport
{
    private final LocalPeer localPeer;

    @Argument( index = 0, name = "host id", required = false, multiValued = false, description = "host id" )
    String hostId;


    public ListReservedPortsCommand( final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( localPeer );

        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute()
    {
        try
        {
            final ReservedPorts reservedPorts =
                    StringUtils.isBlank( hostId ) ? localPeer.getManagementHost().getReservedPorts() :
                    localPeer.getResourceHostById( hostId ).getReservedPorts();

            for ( ReservedPort reservedPort : reservedPorts.getReservedPorts() )
            {
                System.out.format( "%s:%s%n", reservedPort.getProtocol(), reservedPort.getPort() );
            }
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
            log.error( "Error in ListReservedPortsCommand", e );
        }

        return null;
    }
}
