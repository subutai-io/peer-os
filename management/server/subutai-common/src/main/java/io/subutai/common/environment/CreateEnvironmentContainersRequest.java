package io.subutai.common.environment;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.task.CloneRequest;


public class CreateEnvironmentContainersRequest
{
    @JsonProperty( value = "environmentId" )
    private final String environmentId;

    @JsonProperty( value = "initiatorPeerId" )
    private final String initiatorPeerId;

    @JsonProperty( value = "ownerId" )
    private final String ownerId;

    @JsonProperty( value = "requests" )
    private Set<CloneRequest> requests = new HashSet<>();

    @JsonProperty( value = "kurjunToken" )
    private final String kurjunToken;


    public CreateEnvironmentContainersRequest( @JsonProperty( value = "environmentId" ) final String environmentId,
                                               @JsonProperty( value = "initiatorPeerId" ) final String initiatorPeerId,
                                               @JsonProperty( value = "ownerId" ) final String ownerId,
                                               @JsonProperty( value = "kurjunToken" ) final String kurjunToken,
                                               @JsonProperty( value = "requests" ) final Set<CloneRequest> requests )
    {
        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
        this.requests = requests;
        this.kurjunToken = kurjunToken;
    }


    public CreateEnvironmentContainersRequest( final String environmentId, final String initiatorPeerId,
                                               final String ownerId, final String kurjunToken )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( initiatorPeerId ), "Invalid initiator peer id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ownerId ), "Invalid owner id" );

        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
        this.kurjunToken = kurjunToken;
    }


    public void addRequest( final CloneRequest request )
    {
        Preconditions.checkNotNull( request, "Clone request is null" );

        this.requests.add( request );
    }


    public String getKurjunToken()
    {
        return kurjunToken;
    }


    public Set<CloneRequest> getRequests()
    {
        return requests;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getInitiatorPeerId()
    {
        return initiatorPeerId;
    }


    public String getOwnerId()
    {
        return ownerId;
    }
}
