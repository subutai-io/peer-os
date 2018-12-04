package io.subutai.bazaar.share.event.meta;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class CustomMeta implements Meta
{
    private String key;
    private String value;


    public CustomMeta( final String key, final String value )
    {
        this.key = key;
        this.value = value;
    }


    private CustomMeta()
    {
    }


    public String getKey()
    {
        return key;
    }


    public String getValue()
    {
        return value;
    }


    @Override
    public String toString()
    {
        return "StringMetaData{" + "value='" + value + '\'' + '}';
    }
}
