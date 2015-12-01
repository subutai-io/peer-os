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
        @JsonSubTypes.Type( value = QuotaAlertResource.class, name = "ResourceAlertValue" ),
        @JsonSubTypes.Type( value = StringAlertResource.class, name = "StringAlertValue" ),
} )
public interface AlertResource
{
    HostId getHostId();

    Object getValue();

    String getId();

    AlertType getType();
}
