package io.subutai.common.task;


public class ImportTemplateResponse implements TaskResponse<ImportTemplateRequest>
{
    private String resourceHostId;
    private String templateName;
    private boolean succeeded = false;
    private long elapsedTime;


    public ImportTemplateResponse( final String resourceHostId, final String templateName, final boolean succeeded,
                                   final long elapsedTime )
    {
        this.resourceHostId = resourceHostId;
        this.templateName = templateName;
        this.succeeded = succeeded;
        this.elapsedTime = elapsedTime;
    }


    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public String getTemplateName()
    {
        return templateName;
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
        return succeeded ? String.format( "Importing %s succeeded.", templateName ) :
               String.format( "Importing %s failed.", templateName );
    }
}
