package io.subutai.hub.share.event;


import java.util.List;

import io.subutai.hub.share.event.meta.CustomMeta;
import io.subutai.hub.share.event.meta.OriginMeta;
import io.subutai.hub.share.event.payload.Payload;


public interface Event
{
    Payload getPayload();

    void addTrace( String place );

    long getTimestamp();

    OriginMeta getOrigin();

    void addCustomMeta( CustomMeta customMeta );

    List<CustomMeta> getCustomMetaByKey( String key );
}
