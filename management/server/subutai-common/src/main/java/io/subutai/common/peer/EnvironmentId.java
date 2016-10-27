package io.subutai.common.peer;


import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Environment identifier
 */
public class EnvironmentId extends SubutaiId
{
    public EnvironmentId( @JsonProperty( "id" ) final String id )
    {
        super( id );
    }
}
