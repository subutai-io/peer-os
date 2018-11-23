package io.subutai.core.network.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.Protocol;
import io.subutai.common.protocol.ReservedPort;
import io.subutai.common.protocol.ReservedPorts;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "net", name = "container-ports", description = "List container port mappings on host" )
public class ListContainerPortMappingsCommand extends SubutaiShellCommandSupport
{
    private final LocalPeer localPeer;

    @Argument( name = "host id", description = "host id, default mh" )
    String hostId;
    @Argument( index = 1, name = "protocol", description = "protocol: tcp | udp | https | https, default all" )
    String protocolName;


    public ListContainerPortMappingsCommand( final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( localPeer );

        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute()
    {
        try
        {
            Protocol protocol =
                    ( Strings.isNullOrEmpty( protocolName ) || "all".equalsIgnoreCase( protocolName ) ) ? null :
                    Protocol.valueOf( protocolName.toUpperCase() );
            ResourceHost host = ( Strings.isNullOrEmpty( hostId ) || "mh".equalsIgnoreCase( hostId ) ) ?
                                localPeer.getManagementHost() : localPeer.getResourceHostById( hostId );

            final ReservedPorts reservedPorts = host.getContainerPortMappings( protocol );

            for ( ReservedPort reservedPort : reservedPorts.getReservedPorts() )
            {
                System.out.format( "%s %s %s %s%n", reservedPort.getProtocol(), reservedPort.getPort(),
                        reservedPort.getContainerIpPort(),
                        reservedPort.getDomain() == null ? "" : reservedPort.getDomain() );
            }
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
            log.error( "Error in ListContainerPortMappingsCommand", e );
        }

        return null;
    }
}
