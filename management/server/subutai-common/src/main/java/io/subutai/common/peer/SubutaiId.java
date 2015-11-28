package io.subutai.common.peer;


import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Abstract identifier class
 */
public abstract class SubutaiId
{
    @JsonProperty( "id" )
    private String id;


    @JsonCreator
    public SubutaiId( @JsonProperty( "id" ) final String id )
    {
        this.id = id;
    }


    public String getId()
    {
        return id;
    }


    @Override
    public String toString()
    {
        return id;
    }
}
