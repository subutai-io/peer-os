package io.subutai.common.peer;


import org.codehaus.jackson.annotate.JsonProperty;


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
