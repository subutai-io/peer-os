package io.subutai.core.environment.rest.ui.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties( ignoreUnknown = true )
public class ChangedContainerDto
{
    @JsonProperty( value = "key" )
    private String hostId;

    @JsonProperty( value = "value" )
    private ContainerQuotaDto quota;


    public ChangedContainerDto( @JsonProperty( value = "key" ) final String hostId,
                                @JsonProperty( value = "value" ) final ContainerQuotaDto quota )
    {
        this.hostId = hostId;
        this.quota = quota;
    }


    public String getHostId()
    {
        return hostId;
    }


    public ContainerQuotaDto getQuota()
    {
        return quota;
    }
}
