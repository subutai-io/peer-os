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
        @JsonSubTypes.Type( value = QuotaAlert.class, name = "ResourceAlertValue" ),
        @JsonSubTypes.Type( value = StringAlert.class, name = "StringAlertValue" ),
} )
public interface Alert
{
    HostId getHostId();

    <T extends AlertValue> T getAlertValue(Class<T> format);

    String getId();

//    AlertType getType();

    long getCreatedTime();

    long getLiveTime();

    boolean validate();
}
