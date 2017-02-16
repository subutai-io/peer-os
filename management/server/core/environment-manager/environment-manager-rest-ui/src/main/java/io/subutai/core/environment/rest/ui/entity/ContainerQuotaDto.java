package io.subutai.core.environment.rest.ui.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.hub.share.quota.ContainerSize;


@JsonIgnoreProperties( ignoreUnknown = true )
public class ContainerQuotaDto
{
    @JsonProperty( value = "containerSize" )
    private ContainerSize containerSize;

    @JsonProperty( value = "cpuQuota" )
    private String cpu;

    @JsonProperty( value = "ramQuota" )
    private String ram;

    @JsonProperty( value = "homeQuota" )
    private String home;

    @JsonProperty( value = "rootQuota" )
    private String root;

    @JsonProperty( value = "varQuota" )
    private String var;

    @JsonProperty( value = "optQuota" )
    private String opt;


    public ContainerQuotaDto( @JsonProperty( value = "containerSize" ) final ContainerSize containerSize,
                              @JsonProperty( value = "cpuQuota" ) final String cpu,
                              @JsonProperty( value = "ramQuota" ) final String ram,
                              @JsonProperty( value = "homeQuota" ) final String home,
                              @JsonProperty( value = "rootQuota" ) final String root,
                              @JsonProperty( value = "varQuota" ) final String var,
                              @JsonProperty( value = "optQuota" ) final String opt )
    {
        this.containerSize = containerSize;
        this.cpu = cpu;
        this.ram = ram;
        this.home = home;
        this.root = root;
        this.var = var;
        this.opt = opt;
    }


    public ContainerSize getContainerSize()
    {
        return containerSize;
    }


    public String getCpu()
    {
        return cpu;
    }


    public String getRam()
    {
        return ram;
    }


    public String getHome()
    {
        return home;
    }


    public String getRoot()
    {
        return root;
    }


    public String getVar()
    {
        return var;
    }


    public String getOpt()
    {
        return opt;
    }
}
