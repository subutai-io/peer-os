package io.subutai.common.environment;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.gson.required.GsonRequired;
import io.subutai.common.peer.ContainerSize;


/**
 * Node
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class Node
{
    @GsonRequired
    @JsonProperty( "name" )
    private String name;


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

    @JsonProperty( "templateId" )
    private String templateId;


    private Node()
    {
    }


    public Node( @JsonProperty( "hostname" ) final String hostname, @JsonProperty( "name" ) final String name,
                 @JsonProperty( "type" ) ContainerSize type, @JsonProperty( "peerId" ) final String peerId,
                 @JsonProperty( "hostId" ) final String hostId, @JsonProperty( "templateId" ) String templateId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid host name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid node group name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateId ), "Invalid template id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostId ), "Resource host id is null" );
        Preconditions.checkNotNull( type );

        this.hostname = hostname.replaceAll( "\\s+", "" );
        this.name = name;
        this.type = type;
        this.peerId = peerId;
        this.hostId = hostId;
        this.templateId = templateId;
    }


    public String getName()
    {
        return name;
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
        return "Node{" + "name='" + name + '\'' + ", templateId='" + templateId + '\'' + ", type=" + type + ", peerId='"
                + peerId + '\'' + ", hostId='" + hostId + '\'' + ", hostname='" + hostname + '\'' + '}';
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


    public String getTemplateId()
    {
        return templateId;
    }
}
