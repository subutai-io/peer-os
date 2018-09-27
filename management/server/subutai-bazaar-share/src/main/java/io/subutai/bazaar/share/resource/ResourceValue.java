package io.subutai.bazaar.share.resource;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


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
