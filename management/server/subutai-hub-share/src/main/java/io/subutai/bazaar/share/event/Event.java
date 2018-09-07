package io.subutai.bazaar.share.event;


import java.util.List;

import io.subutai.bazaar.share.event.meta.CustomMeta;
import io.subutai.bazaar.share.event.meta.OriginMeta;
import io.subutai.bazaar.share.event.meta.SourceMeta;
import io.subutai.bazaar.share.event.payload.Payload;


public interface Event
{
    Payload getPayload();

    void addTrace( String place );

    long getTimestamp();

    OriginMeta getOrigin();

    SourceMeta getSource();

    void addCustomMeta( CustomMeta customMeta );

    List<CustomMeta> getCustomMetaByKey( String key );
}
