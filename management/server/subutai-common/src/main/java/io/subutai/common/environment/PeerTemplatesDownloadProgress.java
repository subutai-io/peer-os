package io.subutai.common.environment;


import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import io.subutai.common.host.HostId;


public class PeerTemplatesDownloadProgress
{
    @JsonProperty( "peerDownloadMap" )
    Map<HostId, RhTemplatesDownloadProgress> peerTemplatesDownloadProgressMap = Maps.newHashMap();


    @JsonIgnore
    public void addRhTemplateDownloadProgress( String rhId, RhTemplatesDownloadProgress downloadProgress )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( rhId ) );
        Preconditions.checkNotNull( downloadProgress );

        peerTemplatesDownloadProgressMap.put( new HostId( rhId ), downloadProgress );
    }


    @JsonIgnore
    public Map<HostId, RhTemplatesDownloadProgress> getPeerTemplatesDownloadProgressMap()
    {
        return peerTemplatesDownloadProgressMap;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this )
                          .add( "peerTemplatesDownloadProgressMap", peerTemplatesDownloadProgressMap ).toString();
    }
}
