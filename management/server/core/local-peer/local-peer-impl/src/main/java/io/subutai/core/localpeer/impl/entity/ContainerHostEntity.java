package io.subutai.core.localpeer.impl.entity;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.security.objects.PermissionObject;


/**
 * ContainerHost class.
 */
@Entity
@Table( name = "peer_con" )
@Access( AccessType.FIELD )
public class ContainerHostEntity extends AbstractSubutaiHost implements ContainerHost
{

    @ManyToOne( targetEntity = ResourceHostEntity.class )
    @JoinColumn( name = "parent_id" )
    private ResourceHost parent;

    @Column( name = "containerName" )
    private String containerName;

    @Column( name = "env_id", nullable = true )
    private String environmentId;

    @Column( name = "initiator_peer_id", nullable = true )
    private String initiatorPeerId;

    @Column( name = "owner_id", nullable = true )
    private String ownerId;

    @Column( name = "container_type", nullable = true )
    @Enumerated( EnumType.STRING )
    private ContainerSize containerSize = ContainerSize.SMALL;


    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    private Set<String> tags = new HashSet<>();


    @Transient
    private ContainerId containerId;

    @Column( name = "template_name", nullable = false )
    private String templateName;

    @Column( name = "template_arch", nullable = true )
    private String templateArch;


    protected ContainerHostEntity()
    {
    }


    @Override
    public HostId getResourceHostId() throws PeerException
    {
        return new HostId( parent.getId() );
    }


    @Override
    public String getContainerName()
    {
        return containerName;
    }


    public ContainerHostEntity( final String peerId, final String hostId, final String hostname,
                                HostArchitecture architecture, HostInterfaces hostInterfaces,
                                final String containerName, final String templateName, final String templateArch,
                                final String environmentId, final String ownerId, final String initiatorPeerId,
                                final ContainerSize containerSize )
    {
        super( peerId, hostId, hostname, architecture, hostInterfaces );
        this.containerName = containerName;
        this.templateName = templateName;
        this.templateArch = templateArch;
        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
        this.containerSize = containerSize;
    }


    public void setEnvironmentId( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    public EnvironmentId getEnvironmentId()
    {
        return new EnvironmentId( environmentId );
    }


    @Override
    public String getInitiatorPeerId()
    {
        return initiatorPeerId;
    }


    @Override
    public String getOwnerId()
    {
        return ownerId;
    }


    @Override
    public Peer getPeer()
    {
        return parent.getPeer();
    }


    @Override
    public void addTag( final String tag )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tag ) );
        this.tags.add( tag );
    }


    @Override
    public void removeTag( final String tag )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tag ) );
        this.tags.remove( tag );
    }


    @Override
    public Set<String> getTags()
    {
        return this.tags;
    }


    @Override
    public boolean isLocal()
    {
        return true;
    }


    public ContainerHostState getState()
    {
        try
        {
            return getPeer().getContainerState( getContainerId() );
        }
        catch ( PeerException e )
        {
            return ContainerHostState.UNKNOWN;
        }
    }


    public ResourceHost getParent()
    {
        return parent;
    }


    public void setParent( final ResourceHost parent )
    {
        this.parent = parent;
    }


    public String getTemplateName()
    {
        return this.templateName;
    }


    public String getTemplateArch()
    {
        return templateArch;
    }


    public TemplateKurjun getTemplate() throws PeerException
    {
        return getPeer().getTemplate( this.templateName );
    }


    public void dispose() throws PeerException
    {
        getPeer().destroyContainer( getContainerId() );
    }


    @Override
    public void start() throws PeerException
    {
        Peer peer = getPeer();
        peer.startContainer( getContainerId() );
    }


    @Override
    public void stop() throws PeerException
    {
        Peer peer = getPeer();
        peer.stopContainer( getContainerId() );
    }


    @Override
    public boolean updateHostInfo( final HostInfo hostInfo )
    {
        return super.updateHostInfo( hostInfo );
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final int processPid ) throws PeerException
    {
        Peer peer = getPeer();
        return peer.getProcessResourceUsage( getContainerId(), processPid );
    }


//    @Override
//    public ContainerQuota getAvailableQuota() throws PeerException
//    {
//        return getPeer().getAvailableQuota( this.getContainerId() );
//    }


    @Override
    public ContainerQuota getQuota() throws PeerException
    {
        return getPeer().getQuota( this.getContainerId() );
    }


    @Override
    public void setQuota( final ContainerQuota containerQuota ) throws PeerException
    {
        getPeer().setQuota( this.getContainerId(), containerQuota );
    }


    @Override
    public Set<Integer> getCpuSet() throws PeerException
    {
        return getPeer().getCpuSet( this );
    }


    @Override
    public void setCpuSet( final Set<Integer> cpuSet ) throws PeerException
    {
        getPeer().setCpuSet( this, cpuSet );
    }


    @Override
    public ContainerSize getContainerSize()
    {
        return containerSize;
    }


    @Override
    public boolean isConnected()
    {
        return ContainerHostState.RUNNING.equals( getState() );
    }


    public ContainerId getContainerId()
    {
        if ( containerId == null )
        {
            containerId = new ContainerId( getId(), getHostname(), new PeerId( getPeerId() ), getEnvironmentId() );
        }
        return containerId;
    }


    @Override
    public String getLinkId()
    {
        return String.format( "%s|%s", getClassPath(), getUniqueIdentifier() );
    }


    @Override
    public String getUniqueIdentifier()
    {
        return getId();
    }


    @Override
    public String getClassPath()
    {
        return this.getClass().getSimpleName();
    }


    @Override
    public String getContext()
    {
        return PermissionObject.PeerManagement.getName();
    }
}
