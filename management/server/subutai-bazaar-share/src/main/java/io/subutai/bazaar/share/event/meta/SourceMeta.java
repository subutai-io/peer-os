package io.subutai.bazaar.share.event.meta;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonAutoDetect( fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE )
public class SourceMeta implements Meta
{
    public enum Type
    {
        CUSTOM, CONTAINER, BLUEPRINT
    }


    private String name;
    @JsonProperty( value = "value" )
    private Type type;


    public SourceMeta( final String name, final Type type )
    {
        this.name = name;
        this.type = type;
    }


    public SourceMeta( final String name )
    {
        this.name = name;
        this.type = Type.CUSTOM;
    }


    private SourceMeta()
    {
    }


    public String getName()
    {
        return name;
    }


    public Type getType()
    {
        return type;
    }
}
