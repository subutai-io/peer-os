package io.subutai.common.task;


public class QuotaResponse implements TaskResponse
{
    private final String resourceHostId;
    private final String containerName;
    private final boolean succeeded;
    private final long elapsedTime;


    public QuotaResponse( final String resourceHostId, final String containerName, final boolean succeeded,
                          final long elapsedTime )
    {
        this.resourceHostId = resourceHostId;
        this.containerName = containerName;
        this.succeeded = succeeded;
        this.elapsedTime = elapsedTime;
    }


    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public boolean hasSucceeded()
    {
        return succeeded;
    }


    @Override
    public long getElapsedTime()
    {
        return elapsedTime;
    }


    @Override
    public String getLog()
    {
        return succeeded ? String.format( "Setting quota %s succeeded.", containerName ) :
               String.format( "Setting quota %s failed.", containerName );
    }
}
