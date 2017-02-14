package io.subutai.common.environment;


import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.hub.share.quota.ContainerQuota;


/**
 * Node group schema
 */
public class NodeSchema
{
    @JsonProperty( "name" )
    private String name;

    @JsonProperty( "quota" )
    private ContainerQuota quota;

    @JsonProperty( "templateName" )
    private String templateName;

    @JsonProperty( "templateId" )
    private String templateId;

    @JsonProperty( "cpuQuota" )
    private double cpu;
    @JsonProperty( "ramQuota" )
    private double ram;
    @JsonProperty( "homeQuota" )
    private double home;
    @JsonProperty( "rootQuota" )
    private double root;
    @JsonProperty( "varQuota" )
    private double var;
    @JsonProperty( "optQuota" )
    private double opt;


    public NodeSchema( @JsonProperty( "name" ) final String name, @JsonProperty( "quota" ) final ContainerQuota quota,
                       @JsonProperty( "templateName" ) final String templateName,
                       @JsonProperty( "templateId" ) final String templateId )
    {
        this.name = name;
        this.quota = quota;
        this.templateName = templateName;
        this.templateId = templateId;
    }


    public double getCpu()
    {
        return cpu;
    }


    public double getRam()
    {
        return ram;
    }


    public double getHome()
    {
        return home;
    }


    public double getRoot()
    {
        return root;
    }


    public double getVar()
    {
        return var;
    }


    public double getOpt()
    {
        return opt;
    }


    public String getName()
    {
        return name;
    }


    public ContainerQuota getQuota()
    {
        return quota;
    }


    @Deprecated
    //This method is deprecated. Use getTemplateId instead
    public String getTemplateName()
    {
        return templateName;
    }


    public String getTemplateId()
    {
        return templateId;
    }
}
