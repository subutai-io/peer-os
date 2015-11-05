package io.subutai.common.peer;


import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;


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
