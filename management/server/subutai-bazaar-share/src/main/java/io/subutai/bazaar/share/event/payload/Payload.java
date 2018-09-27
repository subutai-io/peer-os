package io.subutai.bazaar.share.event.payload;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = CustomPayload.class, name = "custom" ),
        @JsonSubTypes.Type( value = LogPayload.class, name = "log" ),
        @JsonSubTypes.Type( value = ProgressPayload.class, name = "progress" )
} )
@JsonAutoDetect( fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE )
abstract public class Payload
{
    public enum Nature
    {
        CUSTOM, LOG, PROGRESS
    }
}
