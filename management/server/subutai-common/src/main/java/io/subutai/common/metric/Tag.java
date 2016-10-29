package io.subutai.common.metric;


public class Tag
{
    private String name;
    private String value;


    public Tag( final String name, final String value )
    {
        this.name = name;
        this.value = value;
    }


    public String getName()
    {
        return name;
    }


    public String getValue()
    {
        return value;
    }
}
