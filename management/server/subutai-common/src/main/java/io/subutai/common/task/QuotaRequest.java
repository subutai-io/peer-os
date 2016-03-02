package io.subutai.common.task;


import io.subutai.common.peer.ContainerSize;
import io.subutai.common.quota.ContainerQuota;


public class QuotaRequest implements TaskRequest
{
    private final String resourceHostId;
    private final String hostname;
    private final ContainerSize size;


    public QuotaRequest( final String resourceHostId, final String hostname, final ContainerSize size )
    {
        this.resourceHostId = resourceHostId;
        this.hostname = hostname;
        this.size = size;
    }


    public ContainerSize getSize()
    {
        return size;
    }


    public String getHostname()
    {
        return hostname;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "QuotaRequest{" );
        sb.append( "resourceHostId='" ).append( resourceHostId ).append( '\'' );
        sb.append( ", hostname='" ).append( hostname ).append( '\'' );
        sb.append( ", size=" ).append( size );
        sb.append( '}' );
        return sb.toString();
    }


    @Override
    public String getResourceHostId()
    {
        return resourceHostId;
    }
}
