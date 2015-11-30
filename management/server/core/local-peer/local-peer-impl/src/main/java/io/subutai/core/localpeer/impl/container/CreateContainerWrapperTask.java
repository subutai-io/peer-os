package io.subutai.core.localpeer.impl.container;


import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostInfo;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;


public class CreateContainerWrapperTask implements Callable<CreateContainerWrapperTask>
{
    private final ResourceHost resourceHost;
    private final String templateName;
    private final String hostname;
    private final String ip;
    private final int vlan;
    private final String environmentId;
    private final int timeoutSec;
    private ContainerHostInfo hostInfo;


    public CreateContainerWrapperTask( final ResourceHost resourceHost, final String templateName,
                                       final String hostname, final String ip, final int vlan, final int timeoutSec,
                                       final String environmentId )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ) && ip.matches( Common.CIDR_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );
        Preconditions.checkArgument( timeoutSec > 0 );

        this.resourceHost = resourceHost;
        this.templateName = templateName;
        this.hostname = hostname;
        this.ip = ip;
        this.vlan = vlan;
        this.environmentId = environmentId;
        this.timeoutSec = timeoutSec;
    }


    @Override
    public CreateContainerWrapperTask call() throws Exception
    {
        this.hostInfo = resourceHost.createContainer( templateName, hostname, ip, vlan, timeoutSec, environmentId );
        return this;
    }


    public ResourceHost getResourceHost()
    {
        return resourceHost;
    }


    public ContainerHostInfo getHostInfo()
    {
        return hostInfo;
    }
}
