package io.subutai.core.localpeer.cli;


import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.host.HostId;
import io.subutai.common.metric.ExceededQuota;
import io.subutai.common.metric.QuotaAlert;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.protocol.ControlNetworkConfig;
import io.subutai.common.resource.ByteUnit;
import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "control-network-config" )
public class GetControlNetworkCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;


    public GetControlNetworkCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        final ControlNetworkConfig result = localPeer.getControlNetworkConfig();
        System.out.println( String.format( "%s %s %s", result.getPeerId(), result.getFingerprint(), result.getAddress() ));
        System.out.println( "Used networks:" );
        for (String s : result.getUsedNetworks()) {
            System.out.println( s );
        }
        return null;
    }
}
