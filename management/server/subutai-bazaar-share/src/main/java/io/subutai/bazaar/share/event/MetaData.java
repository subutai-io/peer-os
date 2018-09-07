package io.subutai.bazaar.share.event;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import io.subutai.bazaar.share.event.meta.CustomMeta;
import io.subutai.bazaar.share.event.meta.Meta;
import io.subutai.bazaar.share.event.meta.OriginMeta;
import io.subutai.bazaar.share.event.meta.SourceMeta;
import io.subutai.bazaar.share.event.meta.TraceMeta;


@JsonAutoDetect( fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE )
public class MetaData
{
    @JsonProperty( value = "origin" )
    final private OriginMeta origin;

    @JsonProperty( value = "source" )
    final private SourceMeta source;

    private List<Meta> items = new ArrayList<>();


    @JsonCreator
    public MetaData( @JsonProperty( value = "meta" ) final OriginMeta origin,
                     @JsonProperty( value = "source" ) final SourceMeta source )
    {
        this.origin = origin;
        this.source = source;
    }


    public OriginMeta getOrigin()
    {
        return origin;
    }


    public SourceMeta getSource()
    {
        return source;
    }


    public List<CustomMeta> getCustomMetaByKey( String key )
    {
        final List<CustomMeta> result =
                getMetaByObjectClass( CustomMeta.class ).stream().filter( m -> m.getKey().equals( key ) )
                                                        .collect( Collectors.toList() );
        return result;
    }


    List<TraceMeta> getTrace()
    {
        return getMetaByObjectClass( TraceMeta.class );
    }


    private <T extends Meta> List<T> getMetaByObjectClass( Class<T> objectClass )
    {
        final List<T> result = this.items.stream().filter( objectClass::isInstance ).map( objectClass::cast )
                                         .collect( Collectors.toList() );
        return result;
    }


    void addMeta( final Meta meta )
    {
        Preconditions.checkNotNull( meta );
        this.items.add( meta );
    }
}
