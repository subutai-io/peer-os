package io.subutai.common.environment;


import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;


public class PeerTemplatesUploadProgress
{
    @JsonProperty( "peerId" )
    String peerId;

    @JsonProperty( "templatesUploadProgress" )
    List<RhTemplatesUploadProgress> templatesUploadProgress = Lists.newArrayList();


    public PeerTemplatesUploadProgress( @JsonProperty( "peerId" ) final String peerId )
    {
        Preconditions.checkArgument( !StringUtils.isBlank( peerId ) );

        this.peerId = peerId;
    }


    @JsonIgnore
    public void addTemplateUploadProgress( RhTemplatesUploadProgress uploadProgress )
    {
        Preconditions.checkNotNull( uploadProgress );

        templatesUploadProgress.add( uploadProgress );
    }


    @JsonIgnore
    public List<RhTemplatesUploadProgress> getTemplatesUploadProgress()
    {
        return templatesUploadProgress;
    }


    @JsonIgnore
    public String getPeerId()
    {
        return peerId;
    }
}
