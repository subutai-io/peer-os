package io.subutai.common.task;


public class QuotaResponse implements TaskResponse
{
    private String resourceHostId;
    private String hostname;
    private boolean succeeded;
    private long elapsedTime;


    public QuotaResponse( final String resourceHostId, final String hostname, final boolean succeeded,
                          final long elapsedTime )
    {
        this.resourceHostId = resourceHostId;
        this.hostname = hostname;
        this.succeeded = succeeded;
        this.elapsedTime = elapsedTime;
    }


    @Override
    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public String getHostname()
    {
        return hostname;
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
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "QuotaResponse{" );
        sb.append( "resourceHostId='" ).append( resourceHostId ).append( '\'' );
        sb.append( ", hostname='" ).append( hostname ).append( '\'' );
        sb.append( ", succeeded=" ).append( succeeded );
        sb.append( ", elapsedTime=" ).append( elapsedTime );
        sb.append( '}' );
        return sb.toString();
    }
}
