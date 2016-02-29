package io.subutai.common.task;


public class ImportTemplateRequest implements TaskRequest
{
    private final String resourceHostId;
    private final String templateName;


    public ImportTemplateRequest( final String resourceHostId, final String templateName )
    {
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
}
