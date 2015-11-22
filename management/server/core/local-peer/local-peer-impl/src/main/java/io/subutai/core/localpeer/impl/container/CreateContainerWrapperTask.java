package io.subutai.core.localpeer.impl.container;


import java.util.concurrent.Callable;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;
import io.subutai.common.peer.ResourceHost;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class CreateContainerWrapperTask implements Callable<ContainerHost>
{
    private final ResourceHost resourceHost;
    private final String templateName;
    private final String hostname;
    private final String ip;
    private final int vlan;
    private final String gateway;
    private final int timeoutSec;


    public CreateContainerWrapperTask( final ResourceHost resourceHost, final String templateName,
                                       final String hostname, final String ip,
                                       final int vlan, final String gateway, final int timeoutSec )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ) && ip.matches( Common.CIDR_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( gateway ) && gateway.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( timeoutSec > 0 );

        this.resourceHost = resourceHost;
        this.templateName = templateName;
        this.hostname = hostname;
        this.ip = ip;
        this.vlan = vlan;
        this.gateway = gateway;
        this.timeoutSec = timeoutSec;
    }


    @Override
    public ContainerHost call() throws Exception
    {
        return resourceHost.createContainer( templateName, hostname, ip, vlan, gateway, timeoutSec );
    }
}
