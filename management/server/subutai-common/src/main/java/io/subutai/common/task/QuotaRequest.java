package io.subutai.common.task;


import io.subutai.common.peer.ContainerSize;


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
        return "QuotaRequest{" + "resourceHostId='" + resourceHostId + '\'' + ", hostname='" + hostname + '\''
                + ", size=" + size + '}';
    }


    @Override
    public String getResourceHostId()
    {
        return resourceHostId;
    }
}
