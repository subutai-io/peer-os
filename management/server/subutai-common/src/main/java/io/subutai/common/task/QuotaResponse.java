package io.subutai.common.task;


import io.subutai.common.command.CommandResult;


public class QuotaResponse implements TaskResponse<QuotaRequest>
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


//    @Override
//    public void processCommandResult( final QuotaRequest request, final CommandResult commandResult,
//                                      final long elapsedTime )
//    {
//        this.succeeded = commandResult != null && commandResult.hasSucceeded();
//        this.resourceHostId = getResourceHostId();
//        this.hostname = request.getHostname();
//        this.elapsedTime = elapsedTime;
//    }


    @Override
    public String getLog()
    {
        return succeeded ? String.format( "Setting quota %s succeeded.", hostname ) :
               String.format( "Setting quota %s failed.", hostname );
    }
}
