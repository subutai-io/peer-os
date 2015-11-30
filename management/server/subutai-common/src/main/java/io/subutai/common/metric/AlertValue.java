package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import io.subutai.common.host.HostId;


/**
 * Alert value interface
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = ResourceAlertValue.class, name = "ResourceAlertValue" ),
        @JsonSubTypes.Type( value = StringAlertValue.class, name = "StringAlertValue" ),
} )
public interface AlertValue
{
    HostId getHostId();

    Object getValue();

    String getId();

    AlertType getType();
}
