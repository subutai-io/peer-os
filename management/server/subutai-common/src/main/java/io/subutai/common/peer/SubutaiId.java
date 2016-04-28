package io.subutai.common.peer;


import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import io.subutai.common.host.HostId;


/**
 * Abstract identifier class
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = HostId.class, name = "HostId" ),
        @JsonSubTypes.Type( value = EnvironmentId.class, name = "EnvironmentId" )
} )
public abstract class SubutaiId
{
    @JsonProperty( "id" )
    private String id;


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


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof SubutaiId ) )
        {
            return false;
        }

        final SubutaiId subutaiId = ( SubutaiId ) o;

        return !( id != null ? !id.equals( subutaiId.id ) : subutaiId.id != null );
    }


    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}
