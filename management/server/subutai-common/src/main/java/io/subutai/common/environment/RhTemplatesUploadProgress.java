package io.subutai.common.environment;


import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class RhTemplatesUploadProgress
{
    @JsonProperty( "rhId" )
    private String rhId;

    @JsonProperty( "templatesUploadProgress" )
    private Map<String, Integer> templatesUploadProgress;


    public RhTemplatesUploadProgress( @JsonProperty( "rhId" ) final String rhId,
                                      @JsonProperty( "templatesUploadProgress" )
                                      final Map<String, Integer> templatesUploadProgress )
    {
        Preconditions.checkArgument( !StringUtils.isBlank( rhId ) );
        Preconditions.checkNotNull( templatesUploadProgress );

        this.rhId = rhId;
        this.templatesUploadProgress = templatesUploadProgress;
    }


    @JsonIgnore
    public Map<String, Integer> getTemplatesUploadProgresses()
    {
        return templatesUploadProgress;
    }


    @JsonIgnore
    public String getRhId()
    {
        return rhId;
    }
}
