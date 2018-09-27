package io.subutai.bazaar.share.event.meta;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = CustomMeta.class, name = "custom" ),
        @JsonSubTypes.Type( value = TraceMeta.class, name = "trace" ),
        @JsonSubTypes.Type( value = SourceMeta.class, name = "source" ),
        @JsonSubTypes.Type( value = OriginMeta.class, name = "origin" )
} )
public interface Meta
{
}
