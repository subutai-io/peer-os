package io.subutai.bazaar.share.event.meta;


import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonAutoDetect( fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE )
@JsonIgnoreProperties( ignoreUnknown = true )
public class TraceMeta implements Meta
{
    private String place;
    private long timestamp = Instant.now().getEpochSecond();


    public TraceMeta( final String place )
    {
        this.place = place;
    }


    public TraceMeta()
    {
    }


    public String getPlace()
    {
        return place;
    }


    public long getTimestamp()
    {
        return timestamp;
    }


    @Override
    public String toString()
    {
        return "TraceMeta{" + "place='" + place + '\'' + ", timestamp=" + timestamp + '}';
    }
}
