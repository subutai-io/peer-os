package io.subutai.hub.share.dto;


public class SystemConfDto
{
    private SystemConfigurationType key;

    private String value;

    private String description;

    public SystemConfDto()
    {
    }


    public SystemConfDto( final SystemConfigurationType key, final String value, final String description )
    {
        this.key = key;
        this.value = value;
        this.description = description;
    }


    public SystemConfigurationType getKey()
    {
        return key;
    }


    public void setKey( final SystemConfigurationType key )
    {
        this.key = key;
    }


    public String getValue()
    {
        return value;
    }


    public void setValue( final String value )
    {
        this.value = value;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription( final String description )
    {
        this.description = description;
    }
}
