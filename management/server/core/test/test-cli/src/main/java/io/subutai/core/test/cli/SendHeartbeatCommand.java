package io.subutai.core.test.cli;


import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.host.ResourceHostInfoModel;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.broker.api.ByteMessageListener;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "test", name = "heartbeat", description = "Heartbeat sender" )
public class SendHeartbeatCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( SendHeartbeatCommand.class );

    @Argument( index = 0, name = "resourceHostId", required = true, multiValued = false,
            description = "Resource host id to send heartbeat" )
    String resourceHostId;

    @Argument( index = 1, name = "containerId", required = true, multiValued = false,
            description = "Container id to send heartbeat" )
    String containerId;


    @Override
    protected Object doExecute() throws NamingException, HostDisconnectedException
    {

        LocalPeer localPeer = ServiceLocator.getServiceNoCache( LocalPeer.class );
        HostRegistry hostRegistry = ServiceLocator.getServiceNoCache( HostRegistry.class );
        ByteMessageListener byteMessageListener = ServiceLocator.getServiceNoCache( ByteMessageListener.class );

        ContainerHost containerHost = localPeer.findContainerById( new ContainerId( containerId ) );
        ResourceHostInfoModel resourceHostInfo =
                ( ResourceHostInfoModel ) hostRegistry.getResourceHostInfoById( resourceHostId );

        String json1 = JsonUtil.toJson( resourceHostInfo );
        System.out.println( json1 );

        resourceHostInfo.addRamAlert( containerId, "2", "1" );

        String json2 = JsonUtil.toJson( resourceHostInfo );
        System.out.println( json2 );
        json2 = String.format("{\"response\":%s}", json2 );

        byteMessageListener.onMessage( json2.getBytes() );
        return null;
    }
}
