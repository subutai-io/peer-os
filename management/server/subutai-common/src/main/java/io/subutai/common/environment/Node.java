package io.subutai.common.environment;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.gson.required.GsonRequired;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerSize;


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
    @JsonProperty( "quota" )
    private ContainerQuota quota;

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

    private String templateName;


    private Node()
    {
    }


    public Node( @JsonProperty( "hostname" ) final String hostname, @JsonProperty( "name" ) final String name,
                 @JsonProperty( "quota" ) ContainerQuota quota, @JsonProperty( "peerId" ) final String peerId,
                 @JsonProperty( "hostId" ) final String hostId, @JsonProperty( "templateId" ) String templateId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid host name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid node group name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateId ), "Invalid template id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostId ), "Resource host id is null" );
        Preconditions.checkNotNull( quota );

        this.hostname = hostname.replaceAll( "\\s+", "" );
        this.name = name;
        this.quota = quota;
        this.peerId = peerId;
        this.hostId = hostId;
        this.templateId = templateId;
    }


    public String getName()
    {
        return name;
    }


    public ContainerQuota getQuota()
    {
        return quota;
    }


    public void setDefaultQuota()
    {
        this.quota = ContainerSize.TINY.getDefaultContainerQuota();
    }


    @Override
    public String toString()
    {
        return "Node{" + "name='" + name + '\'' + ", templateId='" + templateId + '\'' + ", quota=" + quota
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


    public String getTemplateId()
    {
        return templateId;
    }


    public void setTemplateId( final String templateId )
    {
        this.templateId = templateId;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
    }
}
