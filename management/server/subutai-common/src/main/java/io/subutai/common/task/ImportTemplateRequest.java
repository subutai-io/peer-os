package io.subutai.common.task;


import com.google.common.base.Preconditions;


public class ImportTemplateRequest implements TaskRequest
{
    private final String resourceHostId;
    private final String templateName;


    public ImportTemplateRequest( final String resourceHostId, final String templateName )
    {
        Preconditions.checkNotNull( resourceHostId );
        Preconditions.checkNotNull( templateName );

        this.resourceHostId = resourceHostId;
        this.templateName = templateName;
    }


    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "ImportTemplateRequest{" );
        sb.append( "resourceHostId='" ).append( resourceHostId ).append( '\'' );
        sb.append( ", templateName='" ).append( templateName ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}
