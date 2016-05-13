package io.subutai.common.protocol;


import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.network.ProxyLoadBalanceStrategy;


/**
 * DTO object for reverse proxy config
 */
public class ReverseProxyConfig
{
    @JsonProperty( "environmentId" )
    private String environmentId;
    @JsonProperty( "containerId" )
    private String containerId;
    @JsonProperty( "domainName" )
    private String domainName;
    @JsonProperty( "sslCertPath" )
    private String sslCertPath;
    @JsonProperty( "loadBalanceStrategy" )
    private ProxyLoadBalanceStrategy loadBalanceStrategy;


    public ReverseProxyConfig( @JsonProperty( "environmentId" ) final String environmentId,
                               @JsonProperty( "containerId" ) final String containerId,
                               @JsonProperty( "domainName" ) final String domainName,
                               @JsonProperty( "sslCertPath" ) final String sslCertPath,
                               @JsonProperty( "loadBalanceStrategy" )
                               final ProxyLoadBalanceStrategy loadBalanceStrategy )
    {
        this.environmentId = environmentId;
        this.containerId = containerId;
        this.domainName = domainName;
        this.sslCertPath = sslCertPath;
        this.loadBalanceStrategy = loadBalanceStrategy;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public String getDomainName()
    {
        return domainName;
    }


    public String getSslCertPath()
    {
        return sslCertPath;
    }


    public ProxyLoadBalanceStrategy getLoadBalanceStrategy()
    {
        return loadBalanceStrategy;
    }
}
