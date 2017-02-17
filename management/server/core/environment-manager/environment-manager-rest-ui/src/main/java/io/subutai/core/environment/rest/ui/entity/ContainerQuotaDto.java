package io.subutai.core.environment.rest.ui.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.hub.share.quota.ContainerCpuResource;
import io.subutai.hub.share.quota.ContainerHomeResource;
import io.subutai.hub.share.quota.ContainerOptResource;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.ContainerRamResource;
import io.subutai.hub.share.quota.ContainerRootfsResource;
import io.subutai.hub.share.quota.ContainerSize;
import io.subutai.hub.share.quota.ContainerVarResource;
import io.subutai.hub.share.quota.Quota;


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


    @JsonIgnore
    public ContainerQuota getContainerQuota()
    {
        ContainerQuota quota = new ContainerQuota( this.containerSize );
        quota.add( new Quota( new ContainerCpuResource( this.getCpu() ), 0 ) );
        quota.add( new Quota( new ContainerRamResource( this.getRam() ), 0 ) );
        quota.add( new Quota( new ContainerRootfsResource( this.getRoot() ), 0 ) );
        quota.add( new Quota( new ContainerHomeResource( this.getHome() ), 0 ) );
        quota.add( new Quota( new ContainerOptResource( this.getOpt() ), 0 ) );
        quota.add( new Quota( new ContainerVarResource( this.getVar() ), 0 ) );
        return quota;
    }
}
