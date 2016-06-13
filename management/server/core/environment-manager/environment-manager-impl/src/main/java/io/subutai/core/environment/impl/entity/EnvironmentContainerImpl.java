package io.subutai.core.environment.impl.entity;


import java.io.Serializable;
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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.util.StringUtil;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.adapter.EnvironmentAdapter;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;


/**
 * Database entity to store environment container host parameters in structured manner.
 */
@Entity
@Table( name = "env_con" )
@Access( AccessType.FIELD )
public class EnvironmentContainerImpl implements EnvironmentContainerHost, Serializable
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

    @Column( name = "containerName", nullable = true )
    @JsonProperty( "containerName" )
    private String containerName;

    @Column( name = "creator_peer_id", nullable = false )
    @JsonIgnore
    private String creatorPeerId;

    @Column( name = "template_name", nullable = false )
    @JsonProperty( "template" )
    private String templateName;

    @Column( name = "template_arch", nullable = false )
    @JsonIgnore
    private HostArchitecture templateArch;

    @Column( name = "rh_id", nullable = false )
    @JsonProperty( "resourceHostId" )
    private String resourceHostId;

    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    @JsonIgnore
    private Set<String> tags = new HashSet<>();

    @ManyToOne( targetEntity = EnvironmentImpl.class, fetch = FetchType.EAGER )
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
    protected Set<HostInterface> hostInterfaces = new HashSet<>();

    @Column( name = "ssh_group_id" )
    @JsonIgnore
    private int sshGroupId;

    @Column( name = "hosts_group_id" )
    @JsonIgnore
    private int hostsGroupId;

    @Column( name = "domain_name" )
    @JsonProperty( "domainName" )
    private String domainName;

    @Column( name = "type" )
    @Enumerated( EnumType.STRING )
    @JsonProperty( "size" )
    private ContainerSize containerSize;

    @Transient
    @JsonIgnore
    protected EnvironmentManagerImpl environmentManager;

    @Transient
    @JsonIgnore
    private ContainerId containerId;

    //workaround for JPA problem setting parent environment field
    @Transient
    private Environment parent;

    private EnvironmentAdapter envAdapter;


    protected EnvironmentContainerImpl()
    {
    }


    public void init()
    {
        // Empty method
    }


    public EnvironmentContainerImpl( final String creatorPeerId, final String peerId,
                                     final ContainerHostInfoModel hostInfo, final String templateName,
                                     final HostArchitecture templateArch, int sshGroupId, int hostsGroupId,
                                     String domainName, ContainerSize containerSize, String resourceHostId,
                                     final String containerName )
    {
        Preconditions.checkNotNull( peerId );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domainName ) );
        Preconditions.checkNotNull( hostInfo );
        Preconditions.checkNotNull( templateName );
        Preconditions.checkNotNull( containerSize );

        this.creatorPeerId = creatorPeerId;
        this.peerId = peerId;
        this.hostId = hostInfo.getId();
        this.hostname = hostInfo.getHostname();
        this.containerName = containerName;
        this.hostArchitecture = hostInfo.getArch();
        this.templateName = templateName;
        this.templateArch = templateArch;
        this.sshGroupId = sshGroupId;
        this.hostsGroupId = hostsGroupId;
        this.domainName = domainName;
        this.containerSize = containerSize;
        this.resourceHostId = resourceHostId;
        setHostInterfaces( hostInfo.getHostInterfaces() );
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
        return getPeer().isLocal();
    }


    @Override
    public EnvironmentId getEnvironmentId()
    {
        return parent.getEnvironmentId();
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
            logger.warn( "Error getting container state #getState" );
            return ContainerHostState.UNKNOWN;
        }
    }


    @Override
    public String getContainerName()
    {
        return containerName;
    }


    @Override
    public void dispose() throws PeerException
    {
        try
        {
            environmentManager.destroyContainer( parent.getId(), this.getId(), false );
        }
        catch ( EnvironmentNotFoundException | EnvironmentModificationException e )
        {
            throw new PeerException( e );
        }
    }


    public Environment destroy() throws PeerException
    {
        getPeer().destroyContainer( getContainerId() );

        ( ( EnvironmentImpl ) parent ).removeContainer( this );

        Environment env = environmentManager.update( ( EnvironmentImpl ) parent );

        environment = null;

        environmentManager.notifyOnContainerDestroyed( env, getId() );

        return env;
    }


    @Override
    public void start() throws PeerException
    {
        getPeer().startContainer( getContainerId() );

        envAdapter.onContainerStart( environment.getId(), getId() );
    }


    @Override
    public void stop() throws PeerException
    {
        getPeer().stopContainer( getContainerId() );

        envAdapter.onContainerStop( environment.getId(), getId() );
    }


    public void setEnvironmentAdapter( EnvironmentAdapter envAdapter )
    {
        this.envAdapter = envAdapter;
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
            throw new RuntimeException( e.getMessage(), e );
        }
    }


    @Override
    public TemplateKurjun getTemplate() throws PeerException
    {
        return getPeer().getTemplate( this.templateName );
    }


    @Override
    public String getTemplateName()
    {
        return this.templateName;
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


    @Override
    public String getHostname()
    {
        return this.hostname;
    }


    public EnvironmentContainerHost setHostname( final String hostname ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );
        Preconditions
                .checkArgument( !StringUtil.areStringsEqual( this.hostname, hostname, true ), "No change in hostname" );

        getPeer().setContainerHostname( getContainerId(), hostname );

        this.hostname = hostname;

        return environmentManager.update( this );
    }


    private void validateTrustChain() throws CommandException
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


    public void setHostInterfaces( HostInterfaces hostInterfaces )
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
    public ProcessResourceUsage getProcessResourceUsage( final int processPid ) throws PeerException
    {
        return getPeer().getProcessResourceUsage( getContainerId(), processPid );
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


    public int getSshGroupId()
    {
        return sshGroupId;
    }


    public int getHostsGroupId()
    {
        return hostsGroupId;
    }


    public String getDomainName()
    {
        return domainName;
    }


    protected void setHostId( String id )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( id ) );
        this.hostId = id;
    }


    @Override
    public String getInitiatorPeerId()
    {
        return this.peerId;
    }


    @Override
    public String getOwnerId()
    {
        throw new UnsupportedOperationException( "Not implemented yet." );
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
            containerId = new ContainerId( getId(), getHostname(), new PeerId( getPeerId() ), getEnvironmentId() );
        }
        return containerId;
    }


    @Override
    public ContainerSize getContainerSize()
    {
        return containerSize;
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
                          .add( "creatorPeerId", creatorPeerId ).add( "templateName", templateName )
                          .add( "environmentId", envId ).add( "sshGroupId", sshGroupId )
                          .add( "hostsGroupId", hostsGroupId ).add( "domainName", domainName ).add( "tags", tags )
                          .add( "templateArch", templateArch ).add( "hostArchitecture", hostArchitecture )
                          .add( "resourceHostId", resourceHostId ).toString();
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
        return PermissionObject.EnvironmentManagement.getName();
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
}
