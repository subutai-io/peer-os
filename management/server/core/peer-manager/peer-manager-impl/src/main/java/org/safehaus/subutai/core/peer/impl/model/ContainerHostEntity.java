package org.safehaus.subutai.core.peer.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.ContainerState;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.ResourceHost;


/**
 * ContainerHost class.
 */
@Entity
@DiscriminatorValue( "C" )
@Access( AccessType.FIELD )
public class ContainerHostEntity extends AbstractSubutaiHost implements ContainerHost
{
    @OneToMany
    @JoinColumn( name = "parent_id" )
    private ResourceHost parent;
    @Column( name = "env_id", nullable = false )
    private String environmentId;
    @Column( name = "creator_id", nullable = false )
    private String creatorPeerId;
    @Column( name = "template_name", nullable = false )
    private String templateName;
    @Column( name = "template_arch", nullable = false )
    private String templateArch;
    @Enumerated( EnumType.STRING )
    private ContainerState state = ContainerState.UNKNOWN;
    @Column( name = "node_group_name", nullable = false )
    private String nodeGroupName;


    public ContainerHostEntity( String peerId, String creatorPeerId, String environmentId, HostInfo hostInfo )
    {
        super( peerId, hostInfo );
        this.creatorPeerId = creatorPeerId;
        this.environmentId = environmentId;
    }


    public String getNodeGroupName()
    {
        return nodeGroupName;
    }


    public void setNodeGroupName( final String nodeGroupName )
    {
        this.nodeGroupName = nodeGroupName;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    public String getCreatorPeerId()
    {
        return creatorPeerId;
    }


    public void setCreatorPeerId( final String creatorPeerId )
    {
        this.creatorPeerId = creatorPeerId;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getTemplateArch()
    {
        return templateArch;
    }


    public void setTemplateArch( final String templateArch )
    {
        this.templateArch = templateArch;
    }


    public ContainerState getState()
    {
        return state;
    }


    public void setState( final ContainerState state )
    {
        this.state = state;
    }


    public ResourceHost getParent()
    {
        return parent;
    }


    public void setParent( final ResourceHost parent )
    {
        this.parent = parent;
    }


    public String getQuota( final QuotaEnum quota ) throws PeerException
    {
        Peer peer = getPeer();
        return peer.getQuota( this, quota );
    }


    public void setQuota( final QuotaEnum quota, final String value ) throws PeerException
    {
        Peer peer = getPeer();
        peer.setQuota( this, quota, value );
    }


    public Template getTemplate() throws PeerException
    {
        Peer peer = getPeer();
        return peer.getTemplate( getTemplateName() );
    }


    public void dispose() throws PeerException
    {
        Peer peer = getPeer();
        peer.destroyContainer( this );
    }
}
