package io.subutai.common.host;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.SubutaiId;


/**
 * Host identifier class
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = ContainerId.class, name = "ContainerId" )
} )
public class HostId extends SubutaiId
{
    public HostId(  @JsonProperty( "id" )final String id )
    {
        super( id );
    }
}
