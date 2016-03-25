package io.subutai.common.environment;


import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.peer.ContainerSize;


/**
 * Node group schema
 */
public class NodeSchema
{
    @JsonProperty( "name" )
    private String name;

    @JsonProperty( "size" )
    private ContainerSize size;

    @JsonProperty( "templateName" )
    private String templateName;

    @JsonProperty( "sshGroupId" )
    private int sshGroupId;

    @JsonProperty( "hostGroupId" )
    private int hostGroupId;


    public NodeSchema( @JsonProperty( "name" ) final String name, @JsonProperty( "size" ) final ContainerSize size,
                       @JsonProperty( "templateName" ) final String templateName,
                       @JsonProperty( "sshGroupId" ) int sshGroupId,

                       @JsonProperty( "hostGroupId" ) int hostGroupId )
    {
        this.name = name;
        this.size = size;
        this.templateName = templateName;
        this.sshGroupId = sshGroupId;
        this.hostGroupId = hostGroupId;
    }


    public String getName()
    {
        return name;
    }


    public ContainerSize getSize()
    {
        return size;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public int getSshGroupId()
    {
        return sshGroupId;
    }


    public int getHostGroupId()
    {
        return hostGroupId;
    }
}
