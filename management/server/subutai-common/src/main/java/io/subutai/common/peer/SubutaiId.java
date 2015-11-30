package io.subutai.common.peer;


import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import io.subutai.common.host.HostId;
import io.subutai.common.metric.ResourceAlertValue;
import io.subutai.common.metric.StringAlertValue;


/**
 * Abstract identifier class
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = HostId.class, name = "HostId" ),
        @JsonSubTypes.Type( value = ContainerId.class, name = "ContainerId" ),
        @JsonSubTypes.Type( value = EnvironmentId.class, name = "EnvironmentId" )
} )
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
