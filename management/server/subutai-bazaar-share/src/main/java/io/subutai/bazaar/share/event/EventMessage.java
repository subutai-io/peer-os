package io.subutai.bazaar.share.event;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.bazaar.share.event.meta.CustomMeta;
import io.subutai.bazaar.share.event.meta.OriginMeta;
import io.subutai.bazaar.share.event.meta.SourceMeta;
import io.subutai.bazaar.share.event.meta.TraceMeta;
import io.subutai.bazaar.share.event.payload.Payload;


@JsonAutoDetect( fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE )
public class EventMessage implements Event
{
    @JsonProperty( value = "metaData", required = true )
    private MetaData metaData;

    @JsonProperty( value = "payload", required = true )
    private Payload payload;


    @JsonProperty( value = "timestamp" )
    private long timestamp = System.currentTimeMillis();


    public EventMessage( final OriginMeta origin, final SourceMeta source, final Payload payload )
    {
        this.payload = payload;
        this.metaData = new MetaData( origin, source );
    }


    protected EventMessage()
    {
    }


    @Override
    public OriginMeta getOrigin()
    {
        return this.metaData.getOrigin();
    }


    @Override
    public SourceMeta getSource()
    {
        return this.metaData.getSource();
    }


    @Override
    public Payload getPayload()
    {
        return payload;
    }


    @Override
    public long getTimestamp()
    {
        return timestamp;
    }


    @Override
    public void addTrace( final String place )
    {
        this.metaData.addMeta( new TraceMeta( place ) );
    }


    @JsonIgnore
    public List<TraceMeta> getTrace()
    {
        return metaData.getTrace();
    }


    @Override
    public void addCustomMeta( CustomMeta customMeta )
    {
        this.metaData.addMeta( customMeta );
    }


    @Override
    public List<CustomMeta> getCustomMetaByKey( final String key )
    {
        return this.metaData.getCustomMetaByKey( key );
    }
}
