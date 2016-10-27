package io.subutai.common.peer;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Peer identifier
 */
public class PeerId extends SubutaiId
{
    @JsonCreator
    public PeerId( @JsonProperty( "id" ) final String id )
    {
        super( id );
    }
}
