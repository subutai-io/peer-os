package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;


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
    <T> T getValue();

    //TODO: throw ValidationException and return exception descriptions
    boolean validate();
}
