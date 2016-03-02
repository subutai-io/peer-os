package io.subutai.common.environment;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.gson.required.GsonRequired;
import io.subutai.common.peer.ContainerSize;


/**
 * Node
 */
public class NodeGroup
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
    @JsonProperty( "sshGroupId" )
    private int sshGroupId;
    @GsonRequired
    @JsonProperty( "hostsGroupId" )
    private int hostsGroupId;
    @GsonRequired
    @JsonProperty( "peerId" )
    private String peerId;
    @GsonRequired
    @JsonProperty( "hostId" )
    private String hostId;


    public NodeGroup( @JsonProperty( "name" ) final String name,
                      @JsonProperty( "templateName" ) final String templateName,
                      @JsonProperty( "type" ) ContainerSize type, @JsonProperty( "sshGroupId" ) final int sshGroupId,
                      @JsonProperty( "hostsGroupId" ) final int hostsGroupId,
                      @JsonProperty( "peerId" ) final String peerId, @JsonProperty( "hostId" ) final String hostId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid node group name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ), "Invalid peer id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostId ), "Invalid host id" );
        Preconditions.checkNotNull( type );

        this.name = name;
        this.templateName = templateName;
        this.type = type;
        this.sshGroupId = sshGroupId;
        this.hostsGroupId = hostsGroupId;
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


    public int getSshGroupId()
    {
        return sshGroupId;
    }


    public int getHostsGroupId()
    {
        return hostsGroupId;
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
        final StringBuffer sb = new StringBuffer( "NodeGroup{" );
        sb.append( "name='" ).append( name ).append( '\'' );
        sb.append( ", templateName='" ).append( templateName ).append( '\'' );
        sb.append( ", type=" ).append( type );
        sb.append( ", sshGroupId=" ).append( sshGroupId );
        sb.append( ", hostsGroupId=" ).append( hostsGroupId );
        sb.append( ", peerId='" ).append( peerId ).append( '\'' );
        sb.append( ", hostId='" ).append( hostId ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}
