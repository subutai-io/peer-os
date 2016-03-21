package io.subutai.core.localpeer.cli;


import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.host.HostId;
import io.subutai.common.metric.ExceededQuota;
import io.subutai.common.metric.QuotaAlert;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.resource.ByteUnit;
import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "put-alert" )
public class PutAlertCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;

    @Argument( index = 0, name = "container ID", multiValued = false, required = true, description = "Container ID" )
    private String containerId;

    @Argument( index = 1, name = "alert type", multiValued = false, required = true, description =
            "Alert type:  \"ram\" , \"cpu\" ,  \"diskOpt\" ," + "    \"diskHome\" ,  \"diskVar\" ,\"diskRootfs\"" )
    private String type;


    public PutAlertCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        final ContainerHost host = localPeer.findContainerById( new ContainerId( containerId ) );


        QuotaAlertValue alertValue = new QuotaAlertValue(
                new ExceededQuota( new HostId( host.getId() ), ContainerResourceType.parse( type ),
                        new ByteValueResource( ByteValueResource.toBytes( "2.3", ByteUnit.MB ) ),
                        new ByteValueResource( ByteValueResource.toBytes( "2.2", ByteUnit.MB ) ) ) );
        QuotaAlert value = new QuotaAlert( alertValue, System.currentTimeMillis() );
        AlertEvent alertEvent = new AlertEvent( localPeer.getId(), host.getEnvironmentId().getId(), host.getId(),
                host.getTemplateName(), value, DateUtils.addMinutes( new Date(), 1 ).getTime() );
        localPeer.alert( alertEvent );
        return null;
    }
}
