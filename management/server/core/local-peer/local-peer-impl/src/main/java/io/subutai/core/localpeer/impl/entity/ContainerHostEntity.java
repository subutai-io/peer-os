package io.subutai.core.localpeer.impl.entity;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.hub.share.quota.ContainerSize;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.Template;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.settings.Common;
import io.subutai.common.util.ServiceLocator;
import io.subutai.hub.share.quota.ContainerQuota;


/**
 * ContainerHost class.
 */
@Entity
@Table( name = "peer_con" )
@Access( AccessType.FIELD )
public class ContainerHostEntity extends AbstractSubutaiHost implements ContainerHost

{
    private static final Logger logger = LoggerFactory.getLogger( ContainerHostEntity.class );


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

    @Transient
    private ContainerId containerId;

    @Column( name = "template_id", nullable = false )
    private String templateId;


    protected ContainerHostEntity()
    {
    }


    @Override
    public HostId getResourceHostId()
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
                                final String containerName, final String templateId, final String environmentId,
                                final String ownerId, final String initiatorPeerId,
                                final ContainerQuota containerQuota )
    {
        super( peerId, hostId, hostname, architecture, hostInterfaces );
        this.containerName = containerName;
        this.templateId = templateId;
        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
        this.containerSize = containerQuota.getContainerSize();
    }


    @Override
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
    public boolean isLocal()
    {
        return true;
    }


    @Override
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


    protected LocalPeer getLocalPeer()
    {
        return ServiceLocator.lookup( LocalPeer.class );
    }


    @Override
    public String getTemplateName()
    {
        try
        {
            return getTemplate().getName();
        }
        catch ( PeerException e )
        {
            logger.error( "Failed to get template by id", e.getMessage() );
        }

        return null;
    }


    @Override
    public String getTemplateId()
    {
        return templateId;
    }


    @Override
    public Template getTemplate() throws PeerException
    {
        return getLocalPeer().getTemplateById( templateId );
    }


    @Override
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
    public void updateHostInfo( final HostInfo hostInfo )
    {
        super.updateHostInfo( hostInfo );

        this.containerName = ( ( ContainerHostInfo ) hostInfo ).getContainerName();
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final int processPid ) throws PeerException
    {
        Peer peer = getPeer();
        return peer.getProcessResourceUsage( getContainerId(), processPid );
    }


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
    public ContainerSize getContainerSize()
    {
        return containerSize;
    }


    @Override
    public void setContainerQuota( final ContainerQuota containerQuota ) throws PeerException
    {
        this.containerSize = containerQuota.getContainerSize();
        setQuota( containerQuota );
    }

    @Override
    public void setContainerSize( final ContainerSize containerSize )
    {
        Preconditions.checkNotNull( containerSize );

        this.containerSize = containerSize;
    }


    @Override
    public boolean isConnected()
    {
        return ContainerHostState.RUNNING.equals( getState() );
    }


    @Override
    public ContainerId getContainerId()
    {
        if ( containerId == null )
        {
            containerId = new ContainerId( getId(), getHostname(), new PeerId( getPeerId() ), getEnvironmentId(),
                    getContainerName() );
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
        return PermissionObject.PEER_MANAGEMENT.getName();
    }


    @Override
    public String getKeyId()
    {
        return getId();
    }


    @Override
    public String getIp()
    {
        return getHostInterfaces().findByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp();
    }
}
