package io.subutai.bazaar.share.resource;


import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * String value resource
 */
public class StringValueResource implements ResourceValue<String>
{
    @JsonProperty( "value" )
    protected String value;


    public StringValueResource( @JsonProperty( "value" ) final String value )
    {
        this.value = value;
    }


    @Override
    public String getValue()
    {
        return value;
    }


    @Override
    public String toString()
    {
        return "StringValueResource{" + "value=" + value + '}';
    }
}
