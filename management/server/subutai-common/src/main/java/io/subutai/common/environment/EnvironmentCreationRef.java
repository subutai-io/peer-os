package io.subutai.common.environment;


import org.codehaus.jackson.annotate.JsonProperty;

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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( trackerId ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );

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
