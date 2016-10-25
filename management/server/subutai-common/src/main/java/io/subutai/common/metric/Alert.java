package io.subutai.common.metric;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.subutai.common.host.HostId;


/**
 * Alert value interface
 */
@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = QuotaAlert.class, name = "QuotaAlert" ),
        @JsonSubTypes.Type( value = StringAlert.class, name = "StringAlert" ),
} )
public interface Alert
{
    HostId getHostId();

    <T extends AlertValue> T getAlertValue( Class<T> format );

    String getId();


    long getCreatedTime();

    long getLiveTime();

    boolean validate();
}
