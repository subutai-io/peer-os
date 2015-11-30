package io.subutai.common.host;


import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.peer.SubutaiId;


/**
 * Host identifier class
 */
public class HostId extends SubutaiId
{
    public HostId( @JsonProperty( "id" ) final String id )
    {
        super( id );
    }


}
