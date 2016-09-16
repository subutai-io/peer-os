package io.subutai.common.environment;


import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class PeerTemplatesDownloadProgress
{
    @JsonProperty( "peerId" )
    String peerId;

    @JsonProperty( "templatesDownloadProgress" )
    Set<RhTemplatesDownloadProgress> templatesDownloadProgress = Sets.newHashSet();


    public PeerTemplatesDownloadProgress( @JsonProperty( "peerId" ) final String peerId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

        this.peerId = peerId;
    }


    @JsonIgnore
    public void addTemplateDownloadProgress( RhTemplatesDownloadProgress downloadProgress )
    {
        Preconditions.checkNotNull( downloadProgress );

        templatesDownloadProgress.add( downloadProgress );
    }


    @JsonIgnore
    public Set<RhTemplatesDownloadProgress> getTemplatesDownloadProgresses()
    {
        return templatesDownloadProgress;
    }


    @JsonIgnore
    public String getPeerId()
    {
        return peerId;
    }
}
