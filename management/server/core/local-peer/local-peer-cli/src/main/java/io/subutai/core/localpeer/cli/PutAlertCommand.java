package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.host.HostId;
import io.subutai.common.metric.ResourceAlert;
import io.subutai.common.metric.ResourceAlertValue;
import io.subutai.common.peer.AlertPack;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.resource.MeasureUnit;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "put-alert" )
public class PutAlertCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;


    public PutAlertCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        ResourceAlert alertValue =
                new ResourceAlert( new HostId( "hostId" ), ResourceType.RAM, new ResourceValue( "1.1", MeasureUnit.MB ),
                        new ResourceValue( "2.2", MeasureUnit.MB ) );
        ResourceAlertValue value = new ResourceAlertValue( alertValue );
        AlertPack alertPack = new AlertPack( localPeer.getId(), "enironmentId", "containerId", "master", value );
        localPeer.putAlert( alertPack );
        return null;
    }
}
