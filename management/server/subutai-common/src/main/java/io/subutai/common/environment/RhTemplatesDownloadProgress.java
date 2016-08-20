package io.subutai.common.environment;


import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class RhTemplatesDownloadProgress
{
    @JsonProperty( "rhId" )
    private String rhId;

    @JsonProperty( "templatesDownloadProgress" )
    private Map<String, Integer> templatesDownloadProgress;


    public RhTemplatesDownloadProgress( @JsonProperty( "rhId" ) final String rhId,
                                        @JsonProperty( "templatesDownloadProgress" )
                                        final Map<String, Integer> templatesDownloadProgress )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( rhId ) );
        Preconditions.checkNotNull( templatesDownloadProgress );

        this.rhId = rhId;
        this.templatesDownloadProgress = templatesDownloadProgress;
    }


    @JsonIgnore
    public Map<String, Integer> getTemplatesDownloadProgresses()
    {
        return templatesDownloadProgress;
    }


    @JsonIgnore
    public String getRhId()
    {
        return rhId;
    }
}
