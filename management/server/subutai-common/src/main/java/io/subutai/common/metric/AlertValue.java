package io.subutai.common.metric;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Alert value interface
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = QuotaAlertValue.class, name = "QuotaAlertValue" ),
        @JsonSubTypes.Type( value = StringAlertValue.class, name = "StringAlertValue" ),
} )
public interface AlertValue<T>
{
    @JsonProperty( "value" )
    T getValue();

    //TODO: throw ValidationException and return exception descriptions
    boolean validate();
}
