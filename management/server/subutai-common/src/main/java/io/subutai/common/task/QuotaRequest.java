package io.subutai.common.task;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.settings.Common;


public class QuotaRequest implements TaskRequest
{
    private final String resourceHostId;
    private final String hostname;
    private final ContainerQuota quota;


    public QuotaRequest( final ContainerQuota quota, final String hostname, final String resourceHostId )
    {
        this.quota = quota;
        this.hostname = hostname;
        this.resourceHostId = resourceHostId;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "QuotaRequest{" );
        sb.append( "resourceHostId='" ).append( resourceHostId ).append( '\'' );
        sb.append( ", hostname='" ).append( hostname ).append( '\'' );
        sb.append( ", quota=" ).append( quota );
        sb.append( '}' );
        return sb.toString();
    }


    @Override
    public String getResourceHostId()
    {
        return resourceHostId;
    }
}
