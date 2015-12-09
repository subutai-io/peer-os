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
        QuotaAlertValue alertValue = new QuotaAlertValue(
                new ExceededQuota( new HostId( "hostId" ), ResourceType.RAM, new ResourceValue( "1.1", MeasureUnit.MB ),
                        new ResourceValue( "2.2", MeasureUnit.MB ) ) );
        QuotaAlert value = new QuotaAlert( alertValue, System.currentTimeMillis() );
        AlertEvent alertEvent = new AlertEvent( localPeer.getId(), "enironmentId", "containerId", "master", value,
                DateUtils.addMinutes( new Date(), 1 ).getTime() );
        localPeer.alert( alertEvent );
        return null;
    }
}
