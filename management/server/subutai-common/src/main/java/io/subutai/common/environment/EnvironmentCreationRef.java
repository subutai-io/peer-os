package io.subutai.common.environment;


import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class EnvironmentCreationRef
{
    @JsonProperty( "trackerId" )
    private String trackerId;
    @JsonProperty( "environmentId" )
    private String environmentId;


    public EnvironmentCreationRef( @JsonProperty( "trackerId" ) final String trackerId,
                                   @JsonProperty( "environmentId" ) final String environmentId )
    {
        Preconditions.checkArgument( !StringUtils.isBlank( trackerId ) );
        Preconditions.checkArgument( !StringUtils.isBlank( environmentId ) );

        this.trackerId = trackerId;
        this.environmentId = environmentId;
    }


    public String getTrackerId()
    {
        return trackerId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }
}
