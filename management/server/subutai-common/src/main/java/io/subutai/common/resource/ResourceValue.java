package io.subutai.common.resource;


import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;


/**
 * Resource value interface
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = ByteValueResource.class, name = "ByteValue" ),
        @JsonSubTypes.Type( value = NumericValueResource.class, name = "NumericValue" ),
} )
public interface ResourceValue<T>
{
    T getValue();
}
