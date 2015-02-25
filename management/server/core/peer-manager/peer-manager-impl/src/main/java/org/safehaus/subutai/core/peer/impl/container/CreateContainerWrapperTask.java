package org.safehaus.subutai.core.peer.impl.container;


import java.util.concurrent.Callable;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.NumUtil;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class CreateContainerWrapperTask implements Callable<ContainerHost>
{
    private final ResourceHost resourceHost;
    private final String templateName;
    private final String hostname;
    private final String ip;
    private final int vlan;
    private final int timeoutSec;


    public CreateContainerWrapperTask( final ResourceHost resourceHost, final String templateName,
                                       final String hostname, final String ip, final int vlan, final int timeoutSec )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );
        Preconditions.checkArgument( timeoutSec > 0 );

        this.resourceHost = resourceHost;
        this.templateName = templateName;
        this.hostname = hostname;
        this.ip = ip;
        this.vlan = vlan;
        this.timeoutSec = timeoutSec;
    }


    @Override
    public ContainerHost call() throws Exception
    {
        return resourceHost.createContainer( templateName, hostname, ip, vlan, timeoutSec );
    }
}
