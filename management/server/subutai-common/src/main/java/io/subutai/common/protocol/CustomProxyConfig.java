package io.subutai.common.protocol;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Custom proxy that allows to specify arbitrary VLAN and domain.
 *
 * Usually used to open some product's Web Console
 */
public class CustomProxyConfig
{
    @JsonProperty
    private String vlan;
    @JsonProperty
    private String domain;
    @JsonProperty
    private String containerId;
    @JsonProperty
    private int port = -1;
    @JsonProperty
    private String environmentId;


    public CustomProxyConfig( @JsonProperty String environmentId, @JsonProperty final String vlan,
                              @JsonProperty final String domain, @JsonProperty final String containerId )
    {
        this.environmentId = environmentId;
        this.vlan = vlan;
        this.domain = domain;
        this.containerId = containerId;
    }


    /**
     * This ctr is used when removing proxy
     */
    public CustomProxyConfig( @JsonProperty final String vlan, @JsonProperty final String containerId,
                              @JsonProperty final String environmentId )
    {
        this.vlan = vlan;
        this.containerId = containerId;
        this.environmentId = environmentId;
    }


    @JsonIgnore
    public CustomProxyConfig setPort( final int port )
    {
        this.port = port;

        return this;
    }


    @JsonIgnore
    public String getEnvironmentId()
    {
        return environmentId;
    }


    @JsonIgnore
    public String getVlan()
    {
        return vlan;
    }


    @JsonIgnore
    public String getDomain()
    {
        return domain;
    }


    public String getContainerId()
    {
        return containerId;
    }


    @JsonIgnore
    public int getPort()
    {
        return port;
    }
}
