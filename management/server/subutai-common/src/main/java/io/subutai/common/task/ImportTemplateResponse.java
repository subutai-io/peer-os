package io.subutai.common.task;


public class ImportTemplateResponse implements TaskResponse<ImportTemplateRequest>
{
    private String resourceHostId;
    private String templateName;
    private boolean succeeded = false;
    private long elapsedTime;
    private String description;


    public ImportTemplateResponse( final String resourceHostId, final String templateName, final boolean succeeded,
                                   final long elapsedTime, final String description )
    {
        this.resourceHostId = resourceHostId;
        this.templateName = templateName;
        this.succeeded = succeeded;
        this.elapsedTime = elapsedTime;
        this.description = description;
    }


    @Override
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


    @Override
    public String getDescription()
    {
        return description;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "ImportTemplateResponse{" );
        sb.append( "resourceHostId='" ).append( resourceHostId ).append( '\'' );
        sb.append( ", templateName='" ).append( templateName ).append( '\'' );
        sb.append( ", succeeded=" ).append( succeeded );
        sb.append( ", elapsedTime=" ).append( elapsedTime );
        sb.append( '}' );
        return sb.toString();
    }
}
