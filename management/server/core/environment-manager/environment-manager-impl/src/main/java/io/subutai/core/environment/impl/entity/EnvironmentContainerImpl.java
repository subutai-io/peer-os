package io.subutai.core.environment.impl.entity;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerSize;
import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.Environment;
import io.subutai.common.exception.ActionFailedException;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.Quota;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.common.protocol.Template;
import io.subutai.common.security.SshKeys;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.settings.Common;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;


/**
 * Database entity to store environment container host parameters in structured manner.
 */
@Entity
@Table( name = "env_con" )
@Access( AccessType.FIELD )
public class EnvironmentContainerImpl implements EnvironmentContainerHost
{
    private static final Logger logger = LoggerFactory.getLogger( EnvironmentContainerImpl.class );

    @Column( name = "peer_id", nullable = false )
    @JsonIgnore
    private String peerId;

    @Id
    @Column( name = "host_id", nullable = false )
    @JsonProperty( "hostId" )
    private String hostId;

    @Column( name = "hostname", nullable = false )
    @JsonProperty( "hostname" )
    private String hostname;

    @Column( name = "containerName" )
    @JsonProperty( "containerName" )
    private String containerName;

    @Column( name = "vlan" )
    @JsonProperty( "vlan" )
    private Integer vlan;


    @Column( name = "creator_peer_id", nullable = false )
    @JsonIgnore
    private String initiatorPeerId;

    @Column( name = "template_id", nullable = false )
    @JsonProperty( "template" )
    private String templateId;


    @Column( name = "rh_id", nullable = false )
    @JsonProperty( "resourceHostId" )
    private String resourceHostId;

    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    @JsonIgnore
    private Set<String> tags = new HashSet<>();

    @ManyToOne( targetEntity = LocalEnvironment.class, fetch = FetchType.EAGER )
    @JoinColumn( name = "environment_id" )
    @JsonIgnore
    protected Environment environment;

    @Column( name = "arch", nullable = false )
    @Enumerated
    @JsonIgnore
    private HostArchitecture hostArchitecture;

    @OneToMany( mappedBy = "host", fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity =
            HostInterfaceImpl.class, orphanRemoval = true )
    @JsonIgnore
    private Set<HostInterface> hostInterfaces = new HashSet<>();


    @Column( name = "domain_name" )
    @JsonProperty( "domainName" )
    private String domainName;

    @Column( name = "type" )
    @Enumerated( EnumType.STRING )
    @JsonProperty( "size" )
    private ContainerSize containerSize;

    @Column
    private Integer domainPort = 80;

    @Column( name = "create_time", nullable = false )
    @JsonProperty( "created" )
    private long creationTimestamp = System.currentTimeMillis();

    @Transient
    @JsonIgnore
    protected transient EnvironmentManagerImpl environmentManager;

    @Transient
    @JsonIgnore
    private ContainerId containerId;

    //workaround for JPA problem setting parent environment field
    @Transient
    private Environment parent;


    protected EnvironmentContainerImpl()
    {
    }


    public void init()
    {
        // Empty method
    }


    public EnvironmentContainerImpl( final String initiatorPeerId, final String peerId,
                                     final ContainerHostInfoModel hostInfo, final String templateId, String domainName,
                                     ContainerQuota containerQuota, String resourceHostId )
    {
        Preconditions.checkNotNull( peerId );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domainName ) );
        Preconditions.checkNotNull( hostInfo );
        Preconditions.checkNotNull( templateId );
        Preconditions.checkNotNull( containerQuota );
        Preconditions.checkNotNull( containerQuota.getContainerSize() );

        this.initiatorPeerId = initiatorPeerId;
        this.peerId = peerId;
        this.hostId = hostInfo.getId();
        this.hostname = hostInfo.getHostname();
        this.containerName = hostInfo.getContainerName();
        this.hostArchitecture = hostInfo.getArch();
        this.templateId = templateId;
        this.domainName = domainName;
        this.containerSize = containerQuota.getContainerSize();
        this.resourceHostId = resourceHostId;
        this.vlan = hostInfo.getVlan();
        setHostInterfaces( hostInfo.getHostInterfaces() );
        this.creationTimestamp = System.currentTimeMillis();
    }


    @Override
    public Quota getRawQuota()
    {
        try
        {
            return getPeer().getRawQuota( getContainerId() );
        }
        catch ( PeerException e )
        {
            logger.error( "Failed to get quota: {}", e.getMessage() );
        }

        return null;
    }


    @Override
    public Integer getVlan()
    {
        return vlan;
    }


    @Override
    public Integer getDomainPort()
    {
        //workaround for existing environments
        if ( domainPort == null )
        {
            return 80;
        }

        return domainPort;
    }


    public void setDomainPort( final Integer domainPort )
    {
        this.domainPort = domainPort;
    }


    public void setEnvironmentManager( final EnvironmentManagerImpl environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    public void setEnvironment( Environment environment )
    {
        Preconditions.checkNotNull( environment );

        this.environment = environment;
        this.parent = environment;
    }


    @Override
    public HostId getResourceHostId()
    {
        return new HostId( resourceHostId );
    }


    @Override
    public boolean isLocal()
    {
        return getPeerId().equals( getLocalPeer().getId() );
    }


    @Override
    public EnvironmentId getEnvironmentId()
    {
        return parent.getEnvironmentId();
    }


    @Override
    public String getEnvId()
    {
        return parent.getId();
    }


    @Override
    public ContainerHostState getState()
    {
        try
        {
            return getPeer().getContainerState( getContainerId() );
        }
        catch ( Exception e )
        {
            logger.warn( "Error getting container state: {}", e.getMessage() );
        }

        return ContainerHostState.UNKNOWN;
    }


    @Override
    public String getHostname()
    {
        return this.hostname;
    }


    @Override
    public String getContainerName()
    {
        return containerName;
    }


    public Environment destroy( boolean removeMetadataOnly ) throws PeerException
    {
        if ( !removeMetadataOnly )
        {
            try
            {
                final Peer peer = getPeer();

                peer.destroyContainer( getContainerId() );
            }
            catch ( Exception e )
            {
                logger.warn( e.getMessage() );
            }
        }

        ( ( LocalEnvironment ) parent ).removeContainer( this );

        if ( parent.getContainerHostsByPeerId( getPeerId() ).isEmpty() )
        {
            ( ( LocalEnvironment ) parent ).removeEnvironmentPeer( getPeerId() );
        }

        Environment env = environmentManager.update( ( LocalEnvironment ) parent );

        environmentManager.notifyOnContainerDestroyed( env, getId() );

        return env;
    }


    public void nullEnvironment()
    {
        environment = null;
    }


    @Override
    public void start() throws PeerException
    {
        getPeer().startContainer( getContainerId() );

        environmentManager.notifyOnContainerStarted( parent, getId() );
    }


    @Override
    public void stop() throws PeerException
    {
        getPeer().stopContainer( getContainerId() );

        environmentManager.notifyOnContainerStopped( parent, getId() );
    }


    @Override
    public Peer getPeer()
    {
        try
        {
            return environmentManager.resolvePeer( peerId );
        }
        catch ( PeerException e )
        {
            throw new ActionFailedException( String.format( "Error resolving peer: %s", e.getMessage() ) );
        }
    }


    protected LocalPeer getLocalPeer()
    {
        return ServiceLocator.lookup( LocalPeer.class );
    }


    @Override
    public Template getTemplate() throws PeerException
    {
        return getLocalPeer().getTemplateById( templateId );
    }


    @Override
    public String getTemplateName()
    {
        try
        {
            return getTemplate().getName();
        }
        catch ( Exception e )
        {
            logger.error( "Failed to get template by id {}: {}", templateId, e.getMessage() );
        }

        return null;
    }


    @Override
    public String getTemplateId()
    {
        return templateId;
    }


    @Override
    public EnvironmentContainerHost addTag( final String tag )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tag ) );

        this.tags.add( tag );

        return environmentManager.update( this );
    }


    @Override
    public EnvironmentContainerHost removeTag( final String tag )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tag ) );

        this.tags.remove( tag );

        return environmentManager.update( this );
    }


    @Override
    public Set<String> getTags()
    {
        return this.tags;
    }


    @Override
    public String getPeerId()
    {
        return this.peerId;
    }


    @Override
    public String getId()
    {
        return hostId;
    }


    public EnvironmentContainerHost setHostname( final String hostname, boolean metadataOnly ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );
        Preconditions
                .checkArgument( !StringUtils.equalsIgnoreCase( this.hostname, hostname ), "No change in hostname" );

        if ( !metadataOnly )
        {
            getPeer().setContainerHostname( getContainerId(), hostname );
        }

        this.hostname = hostname;

        return environmentManager.update( this );
    }


    protected void validateTrustChain() throws CommandException
    {
        if ( environmentManager != null )
        {
            logger.warn( "Trust chain validation is on..." );
            // TODO call relationManager validation here instead
            EnvironmentManagerImpl envImpl = environmentManager;

            IdentityManager identityManager = envImpl.getIdentityManager();
            RelationManager relationManager = envImpl.getRelationManager();

            User activeUser = identityManager.getActiveUser();
            UserDelegate userDelegate = identityManager.getUserDelegate( activeUser );

            if ( activeUser != null )
            {
                RelationMeta relationMeta = new RelationMeta( userDelegate, userDelegate, parent, parent.getId() );
                boolean trustedRelation =
                        relationManager.getRelationInfoManager().groupHasWritePermissions( relationMeta );

                if ( !trustedRelation )
                {
                    throw new CommandException( "Host was revoked to execute commands" );
                }
            }
        }
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder ) throws CommandException
    {
        validateTrustChain();
        return getPeer().execute( requestBuilder, this );
    }


    public CommandResult executeUnsafe( final RequestBuilder requestBuilder ) throws CommandException
    {
        return getPeer().execute( requestBuilder, this );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final CommandCallback callback )
            throws CommandException
    {
        validateTrustChain();
        return getPeer().execute( requestBuilder, this, callback );
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final CommandCallback callback )
            throws CommandException
    {
        validateTrustChain();
        getPeer().executeAsync( requestBuilder, this, callback );
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder ) throws CommandException
    {
        validateTrustChain();
        getPeer().executeAsync( requestBuilder, this );
    }


    @Override
    public boolean isConnected()
    {
        return ContainerHostState.RUNNING.equals( getState() );
    }


    @Override
    public HostInterfaces getHostInterfaces()
    {
        HostInterfaces result = new HostInterfaces();
        for ( HostInterface hostInterface : this.hostInterfaces )
        {
            HostInterfaceModel model = new HostInterfaceModel( hostInterface );
            result.addHostInterface( model );
        }
        return result;
    }


    private void setHostInterfaces( HostInterfaces hostInterfaces )
    {
        Preconditions.checkNotNull( hostInterfaces );

        this.hostInterfaces.clear();
        for ( HostInterface iface : hostInterfaces.getAll() )
        {
            HostInterfaceImpl hostInterface = new HostInterfaceImpl( iface );
            hostInterface.setHost( this );
            this.hostInterfaces.add( hostInterface );
        }
    }


    @Override
    public HostInterface getInterfaceByName( final String interfaceName )
    {
        return getHostInterfaces().findByName( interfaceName );
    }


    @Override
    public HostArchitecture getArch()
    {
        return this.hostArchitecture;
    }


    @Override
    public ContainerQuota getQuota() throws PeerException
    {
        return getPeer().getQuota( this.getContainerId() );
    }


    @Override
    public void setQuota( final ContainerQuota containerQuota ) throws PeerException
    {
        this.containerSize = containerQuota.getContainerSize();
        getPeer().setQuota( this.getContainerId(), containerQuota );
        environmentManager.update( this );
    }


    @Override
    public ContainerSize getContainerSize()
    {
        return containerSize;
    }


    @Override
    public void setContainerSize( final ContainerSize size )
    {
        this.containerSize = size;
    }


    @Override
    public String getInitiatorPeerId()
    {
        return this.initiatorPeerId;
    }


    @Override
    public String getOwnerId()
    {
        throw new UnsupportedOperationException( "Not implemented yet." );
    }


    @Override
    public SshKeys getAuthorizedKeys() throws PeerException
    {
        return getPeer().getContainerAuthorizedKeys( this.getContainerId() );
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof EnvironmentContainerImpl ) )
        {
            return false;
        }

        final EnvironmentContainerImpl container = ( EnvironmentContainerImpl ) o;

        if ( hostId != null ? !hostId.equals( container.getId() ) : container.getId() != null )
        {
            return false;
        }

        return true;
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
    public int hashCode()
    {
        return hostId != null ? hostId.hashCode() : 0;
    }


    @Override
    public String toString()
    {
        String envId = parent != null ? parent.getId() : null;

        return MoreObjects.toStringHelper( this ).add( "hostId", hostId ).add( "hostname", hostname )
                          .add( "initiatorPeerId", initiatorPeerId ).add( "templateId", templateId )
                          .add( "environmentId", envId ).add( "domainName", domainName ).add( "tags", tags )
                          .add( "hostArchitecture", hostArchitecture ).add( "resourceHostId", resourceHostId )
                          .add( "createdAt", new Date( creationTimestamp ) ).toString();
    }


    @Override
    public int compareTo( final HostInfo o )
    {
        if ( hostname != null && o != null )
        {
            return hostname.compareTo( o.getHostname() );
        }
        return -1;
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
        return PermissionObject.ENVIRONMENT_MANAGEMENT.getName();
    }


    @Override
    public String getKeyId()
    {
        return getId();
    }


    public Environment getEnvironment()
    {
        return parent;
    }


    @Override
    public String getIp()
    {
        return getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp();
    }


    @Override
    public long getCreationTimestamp()
    {
        return creationTimestamp;
    }
}
