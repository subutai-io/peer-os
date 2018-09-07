package io.subutai.core.metric.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.host.HostId;
import io.subutai.common.metric.ExceededQuota;
import io.subutai.common.metric.QuotaAlert;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.metric.api.Monitor;
import io.subutai.bazaar.share.resource.ByteUnit;
import io.subutai.bazaar.share.resource.ByteValueResource;
import io.subutai.bazaar.share.resource.ContainerResourceType;


@Command( scope = "alert", name = "put" )
public class PutAlertCommand extends SubutaiShellCommandSupport
{

    private Monitor monitor;

    @Argument( index = 0, name = "container ID", multiValued = false, required = true, description = "Container ID" )
    private String containerId;

    @Argument( index = 1, name = "alert type", multiValued = false, required = true, description =
            "Alert type:  \"ram\" , \"cpu\" ,  \"diskOpt\" ," + "    \"diskHome\" ,  \"diskVar\" ,\"diskRootfs\"" )
    private String type;


    public PutAlertCommand( final Monitor monitor )
    {
        this.monitor = monitor;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        final ResourceHostMetric resourceHostMetric = new ResourceHostMetric();

        QuotaAlertValue alertValue = new QuotaAlertValue(
                new ExceededQuota( new HostId( containerId ), ContainerResourceType.parse( type ),
                        new ByteValueResource( ByteValueResource.toBytes( "2.3", ByteUnit.MB ) ),
                        new ByteValueResource( ByteValueResource.toBytes( "2.2", ByteUnit.MB ) ),
                        resourceHostMetric ) );


        QuotaAlert value = new QuotaAlert( alertValue, System.currentTimeMillis() );
        monitor.putAlert( value );
        return null;
    }
}
