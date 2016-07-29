package io.subutai.common.environment;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.gson.required.GsonRequired;
import io.subutai.common.peer.ContainerSize;


/**
 * Node
 */
public class Node
{
    @GsonRequired
    @JsonProperty( "name" )
    private String name;

    @GsonRequired
    @JsonProperty( "templateName" )
    private String templateName;

    @GsonRequired
    @JsonProperty( "type" )
    private ContainerSize type = ContainerSize.SMALL;

    @GsonRequired
    @JsonProperty( "peerId" )
    private String peerId;

    @GsonRequired
    @JsonProperty( "hostId" )
    private String hostId;

    @GsonRequired
    @JsonProperty( "hostname" )
    private String hostname;


    private Node()
    {
    }


    public Node( @JsonProperty( "hostname" ) final String hostname, @JsonProperty( "name" ) final String name,
                 @JsonProperty( "templateName" ) final String templateName, @JsonProperty( "type" ) ContainerSize type,
                 @JsonProperty( "peerId" ) final String peerId, @JsonProperty( "hostId" ) final String hostId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid host name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid node group name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostId ), "Resource host id is null" );
        Preconditions.checkNotNull( type );

        this.hostname = hostname.replaceAll( "\\s+", "" );
        this.name = name;
        this.templateName = templateName;
        this.type = type;
        this.peerId = peerId;
        this.hostId = hostId;
    }


    public String getName()
    {
        return name;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public ContainerSize getType()
    {
        return type;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getHostId()
    {
        return hostId;
    }


    @Override
    public String toString()
    {
        return "Node{" + "name='" + name + '\'' + ", templateName='" + templateName + '\'' + ", type=" + type
                + ", peerId='" + peerId + '\'' + ", hostId='" + hostId + '\'' + ", hostname='" + hostname + '\'' + '}';
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( final String hostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid host name" );

        this.hostname = hostname.replaceAll( "\\s+", "" );
    }
}
