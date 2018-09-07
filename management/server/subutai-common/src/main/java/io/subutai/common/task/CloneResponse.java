package io.subutai.common.task;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.common.host.HostArchitecture;
import io.subutai.bazaar.share.quota.ContainerQuota;


public class CloneResponse implements TaskResponse
{
    protected static final Logger LOG = LoggerFactory.getLogger( CloneResponse.class );

    @JsonProperty( value = "resourceHostId" )
    private String resourceHostId;

    @JsonProperty( value = "hostname" )
    private String hostname;

    @JsonProperty( value = "templateId" )
    private String templateId;

    @JsonProperty( value = "templateArch" )
    private HostArchitecture templateArch;

    @JsonProperty( value = "containerName" )
    private String containerName;

    @JsonProperty( value = "ip" )
    private String ip;

    @JsonProperty( value = "containerId" )
    private String containerId;

    @JsonProperty( value = "elapsedTime" )
    private long elapsedTime;

    @JsonProperty( value = "containerQuota" )
    private ContainerQuota containerQuota;
    @JsonProperty( value = "vlan" )
    private Integer vlan;


    public CloneResponse( @JsonProperty( value = "resourceHostId" ) final String resourceHostId,
                          @JsonProperty( value = "hostname" ) final String hostname,
                          @JsonProperty( value = "containerName" ) final String containerName,
                          @JsonProperty( value = "templateId" ) final String templateId,
                          @JsonProperty( value = "templateArch" ) final HostArchitecture templateArch,
                          @JsonProperty( value = "ip" ) final String ip,
                          @JsonProperty( value = "containerId" ) final String containerId,
                          @JsonProperty( value = "elapsedTime" ) final long elapsedTime,
                          @JsonProperty( value = "containerQuota" ) final ContainerQuota containerQuota,
                          @JsonProperty( value = "vlan" ) final Integer vlan )
    {
        this.resourceHostId = resourceHostId;
        this.hostname = hostname;
        this.templateId = templateId;
        this.templateArch = templateArch;
        this.containerName = containerName;
        this.ip = ip;
        this.containerId = containerId;
        this.elapsedTime = elapsedTime;
        this.containerQuota = containerQuota;
        this.vlan = vlan;
    }


    @Override
    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public String getHostname()
    {
        return hostname;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public String getIp()
    {
        return ip.split( "/" )[0];
    }


    public String getContainerId()
    {
        return containerId;
    }


    public String getTemplateId()
    {
        return templateId;
    }


    public HostArchitecture getTemplateArch()
    {
        return templateArch;
    }


    @Override
    public long getElapsedTime()
    {
        return elapsedTime;
    }


    public ContainerQuota getContainerQuota()
    {
        return containerQuota;
    }


    public Integer getVlan()
    {
        return vlan;
    }


    @Override
    public String toString()
    {
        return "CloneResponse{" + "resourceHostId='" + resourceHostId + '\'' + ", hostname='" + hostname + '\''
                + ", templateId='" + templateId + '\'' + ", templateArch=" + templateArch + ", containerName='"
                + containerName + '\'' + ", ip='" + ip + '\'' + ", containerId='" + containerId + '\'' + '}';
    }
}
