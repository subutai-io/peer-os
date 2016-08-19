package io.subutai.common.environment;


import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;


public class RhTemplatesDownloadProgress
{
    @JsonProperty( "rhDownloadMap" )
    private Map<String, Integer> templatesDownloadProgressMap;


    public RhTemplatesDownloadProgress(
            @JsonProperty( "rhDownloadMap" ) final Map<String, Integer> templatesDownloadProgressMap )
    {
        Preconditions.checkNotNull( templatesDownloadProgressMap );

        this.templatesDownloadProgressMap = templatesDownloadProgressMap;
    }


    @JsonIgnore
    public Map<String, Integer> getTemplatesDownloadProgressMap()
    {
        return templatesDownloadProgressMap;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this )
                          .add( "peerTemplatesDownloadProgressMap", templatesDownloadProgressMap ).toString();
    }
}
