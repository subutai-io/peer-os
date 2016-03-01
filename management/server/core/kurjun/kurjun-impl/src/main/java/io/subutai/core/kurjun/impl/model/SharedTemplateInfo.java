package io.subutai.core.kurjun.impl.model;


public class SharedTemplateInfo
{

    private String fromUserFprint;
    private String toUserfprint;
    private String templateId;


    public SharedTemplateInfo( String fromUserFprint, String toUserfprint, String templateId )
    {
        this.fromUserFprint = fromUserFprint;
        this.toUserfprint = toUserfprint;
        this.templateId = templateId;
    }


    public String getFromUserFprint()
    {
        return fromUserFprint;
    }


    public String getToUserfprint()
    {
        return toUserfprint;
    }


    public String getTemplateId()
    {
        return templateId;
    }


    @Override
    public String toString()
    {
        return "SharedTemplate{" + "fromUserFprint=" + fromUserFprint + ", toUserfprint=" + toUserfprint + ", templateId=" + templateId + '}';
    }

}
