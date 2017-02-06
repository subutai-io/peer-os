package io.subutai.common.command;


import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;


public class CommandRequest
{
    @JsonProperty( value = "requestBuilder" )
    private RequestBuilder requestBuilder;

    @JsonProperty( value = "hostId" )
    private String hostId;

    @JsonProperty( value = "requestId" )
    private UUID requestId;

    @JsonProperty( value = "environmentId" )
    private String environmentId;


    public CommandRequest( @JsonProperty( value = "requestBuilder" ) final RequestBuilder requestBuilder,
                           @JsonProperty( value = "hostId" ) final String hostId,
                           @JsonProperty( value = "requestId" ) final UUID requestId,
                           @JsonProperty( value = "environmentId" ) final String environmentId )
    {
        this.requestBuilder = requestBuilder;
        this.hostId = hostId;
        this.requestId = requestId;
        this.environmentId = environmentId;
    }


    public CommandRequest( final RequestBuilder requestBuilder, final String hostId, final String environmentId )
    {
        this.requestBuilder = requestBuilder;
        this.hostId = hostId;
        this.environmentId = environmentId;
        this.requestId = UUID.randomUUID();
    }


    public UUID getRequestId()
    {
        return requestId;
    }


    public RequestBuilder getRequestBuilder()
    {
        return requestBuilder;
    }


    public String getHostId()
    {
        return hostId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }
}
