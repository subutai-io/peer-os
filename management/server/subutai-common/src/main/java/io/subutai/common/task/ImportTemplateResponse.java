package io.subutai.common.task;


public class ImportTemplateResponse implements TaskResponse
{
    private String resourceHostId;
    private String templateName;
    private long elapsedTime;


    public ImportTemplateResponse( final String resourceHostId, final String templateName, final long elapsedTime )
    {
        this.resourceHostId = resourceHostId;
        this.templateName = templateName;
        this.elapsedTime = elapsedTime;
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


    @Override
    public long getElapsedTime()
    {
        return elapsedTime;
    }


    @Override
    public String toString()
    {
        return "ImportTemplateResponse{" + "resourceHostId='" + resourceHostId + '\'' + ", templateName='"
                + templateName + '\'' + ", elapsedTime=" + elapsedTime + '}';
    }
}
