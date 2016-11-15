package io.subutai.core.environment.impl.entity;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

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
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentPeer;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentAlertHandler;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;


/**
 * Database entity to store environment specific fields.
 *
 * @see EnvironmentContainerImpl
 * @see ContainerHost
 */
@Entity
@Table( name = "env", uniqueConstraints = @UniqueConstraint( columnNames = { "name", "user_id" } ) )
@Access( AccessType.FIELD )
@JsonAutoDetect( fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE )
public class EnvironmentImpl implements Environment, Serializable
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentImpl.class );

    @Transient
    @JsonIgnore
    private transient EnvironmentManagerImpl environmentManager;

    @Id
    @Column( name = "environment_id" )
    @JsonProperty( "environmentId" )
    protected String environmentId;

    @Column( name = "peer_id", nullable = false )
    @JsonProperty( "peerId" )
    private String peerId;

    @Column( name = "name", nullable = false )
    @JsonProperty( "name" )
    private String name;

    @Column( name = "create_time", nullable = false )
    @JsonProperty( "created" )
    private long creationTimestamp = System.currentTimeMillis();

    @Column( name = "subnet_cidr" )
    @JsonProperty( "subnet" )
    private String subnetCidr;

    @Column( name = "vni" )
    @JsonIgnore
    private Long vni;

    @Column( name = "p2p_subnet" )
    @JsonIgnore
    private String p2pSubnet;

    @Column( name = "p2p_key" )
    @JsonIgnore
    private String p2pKey;

    @OneToMany( mappedBy = "environment", fetch = FetchType.EAGER, targetEntity = EnvironmentContainerImpl.class,
            cascade = CascadeType.ALL, orphanRemoval = true )
    @JsonIgnore
    private Set<EnvironmentContainerHost> containers = Sets.newHashSet();

    @OneToMany( mappedBy = "environment", fetch = FetchType.EAGER, targetEntity = EnvironmentPeerImpl.class, cascade
            = CascadeType.ALL, orphanRemoval = true )
    @JsonIgnore
    private Set<EnvironmentPeer> environmentPeers = Sets.newHashSet();

    @Enumerated( EnumType.STRING )
    @Column( name = "status", nullable = false )
    @JsonProperty( "status" )
    private EnvironmentStatus status = EnvironmentStatus.EMPTY;

    @Column( name = "initial_blueprint" )
    @JsonIgnore
    @Lob
    private String rawBlueprint;

    @Column( name = "user_id" )
    @JsonIgnore
    private Long userId;

    @OneToMany( mappedBy = "environment", fetch = FetchType.EAGER, targetEntity = EnvironmentAlertHandlerImpl.class,
            cascade = CascadeType.ALL, orphanRemoval = true )
    @JsonIgnore
    private Set<EnvironmentAlertHandler> alertHandlers = Sets.newHashSet();


    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    @Column( length = 1000 )
    @JsonIgnore
    private Set<String> sshKeys = new HashSet<>();

    @Transient
    @JsonIgnore
    protected EnvironmentId envId;


    protected EnvironmentImpl()
    {
    }


    public EnvironmentImpl( String name, String sshKey, Long userId, String peerId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

        this.name = name;
        if ( !Strings.isNullOrEmpty( sshKey ) )
        {
            sshKeys.add( sshKey.trim() );
        }
        this.environmentId = UUID.randomUUID().toString();
        this.creationTimestamp = System.currentTimeMillis();
        this.status = EnvironmentStatus.EMPTY;
        this.userId = userId;
        this.peerId = peerId;
    }


    @Override
    public void addSshKey( final String sshKey, final boolean async ) throws EnvironmentModificationException
    {
        try
        {
            environmentManager.addSshKey( getId(), sshKey, async );
        }
        catch ( EnvironmentNotFoundException e )
        {
            //this should not happen
            LOG.error( String.format( "Error adding ssh key to environment %s", getName() ), e );
            throw new EnvironmentModificationException( e );
        }
    }


    @Override
    public void removeSshKey( final String sshKey, final boolean async ) throws EnvironmentModificationException
    {
        try
        {
            environmentManager.removeSshKey( getId(), sshKey, async );
        }
        catch ( EnvironmentNotFoundException e )
        {
            //this should not happen
            LOG.error( String.format( "Error removing ssh key from environment %s", getName() ), e );
            throw new EnvironmentModificationException( e );
        }
    }


    @Override
    public Set<String> getSshKeys()
    {
        return sshKeys;
    }


    public void addSshKey( final String sshKey )
    {
        if ( !Strings.isNullOrEmpty( sshKey ) )
        {
            sshKeys.add( sshKey );
        }
    }


    public void removeSshKey( final String sshKey )
    {
        if ( !Strings.isNullOrEmpty( sshKey ) )
        {
            sshKeys.remove( sshKey );
        }
    }


    @Override
    public long getCreationTimestamp()
    {
        return creationTimestamp;
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public Long getUserId()
    {
        return userId;
    }


    public void setUserId( final Long userId )
    {
        this.userId = userId;
    }


    @Override
    public EnvironmentStatus getStatus()
    {
        return status;
    }


    public void setStatus( EnvironmentStatus status )
    {
        Preconditions.checkNotNull( status );

        this.status = status;
    }


    String getRawTopology()
    {
        return rawBlueprint;
    }


    public void setRawTopology( final String rawBlueprint )
    {
        this.rawBlueprint = rawBlueprint;
    }


    @Override
    public String getPeerId()
    {
        return peerId;
    }


    @Override
    public EnvironmentContainerHost getContainerHostById( String id ) throws ContainerHostNotFoundException
    {
        Preconditions.checkNotNull( id, "Invalid id" );

        for ( final EnvironmentContainerHost containerHost : getContainerHosts() )
        {
            if ( containerHost.getId().equals( id ) )
            {
                return containerHost;
            }
        }
        throw new ContainerHostNotFoundException( String.format( "Container host not found by id %s", id ) );
    }


    @Override
    public Set<EnvironmentContainerHost> getContainerHostsByPeerId( String id )
    {
        Preconditions.checkNotNull( id, "Invalid id" );

        Set<EnvironmentContainerHost> result = new HashSet<>();
        for ( final EnvironmentContainerHost containerHost : getContainerHosts() )
        {
            if ( containerHost.getPeerId().equals( id ) )
            {
                result.add( containerHost );
            }
        }
        return result;
    }


    @Override
    public EnvironmentContainerHost getContainerHostByHostname( String hostname ) throws ContainerHostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );

        for ( final EnvironmentContainerHost containerHost : getContainerHosts() )
        {
            if ( containerHost.getHostname().equalsIgnoreCase( hostname ) )
            {
                return containerHost;
            }
        }
        throw new ContainerHostNotFoundException( String.format( "Container host not found by name %s", hostname ) );
    }


    @Override
    public Set<EnvironmentContainerHost> getContainerHostsByIds( Set<String> ids ) throws ContainerHostNotFoundException
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( ids ), "Invalid id set" );

        Set<EnvironmentContainerHost> hosts = Sets.newHashSet();
        for ( String id : ids )
        {
            hosts.add( getContainerHostById( id ) );
        }
        return hosts;
    }


    public void addEnvironmentPeer( final EnvironmentPeerImpl environmentPeer )
    {

        Preconditions.checkNotNull( environmentPeer, "Environment peer could not be null." );

        environmentPeer.setEnvironment( this );
        environmentPeers.add( environmentPeer );
    }


    void removeEnvironmentPeer( final String peerId )
    {

        Preconditions.checkNotNull( peerId, "Environment peer id could not be null." );

        for ( Iterator<EnvironmentPeer> iterator = environmentPeers.iterator(); iterator.hasNext(); )
        {
            final EnvironmentPeer environmentPeer = iterator.next();

            if ( environmentPeer.getPeerId().equals( peerId ) )
            {
                iterator.remove();
                break;
            }
        }
    }


    public EnvironmentPeerImpl getEnvironmentPeer( String peerId )
    {
        for ( EnvironmentPeer environmentPeer : environmentPeers )
        {
            if ( environmentPeer.getPeerId().equalsIgnoreCase( peerId ) )
            {
                return ( EnvironmentPeerImpl ) environmentPeer;
            }
        }

        return null;
    }


    @Override
    public Set<EnvironmentPeer> getEnvironmentPeers()
    {
        return environmentPeers;
    }


    @Override
    public String getId()
    {
        return environmentId;
    }


    @Override
    public Set<EnvironmentContainerHost> getContainerHosts()
    {
        Set<EnvironmentContainerHost> containerHosts;

        synchronized ( this )
        {
            containerHosts =
                    CollectionUtil.isCollectionEmpty( this.containers ) ? Sets.<EnvironmentContainerHost>newHashSet() :
                    Sets.newConcurrentHashSet( this.containers );
        }

        if ( !CollectionUtil.isCollectionEmpty( containerHosts ) && environmentManager != null )
        {
            RelationManager relationManager = environmentManager.getRelationManager();
            IdentityManager identityManager = environmentManager.getIdentityManager();
            User activeUser = identityManager.getActiveUser();
            UserDelegate userDelegate = identityManager.getUserDelegate( activeUser );


            if ( userDelegate != null )
            {
                for ( Iterator<EnvironmentContainerHost> iterator = containerHosts.iterator(); iterator.hasNext(); )
                {
                    final EnvironmentContainerHost containerHost = iterator.next();
                    RelationMeta relationMeta =
                            new RelationMeta( userDelegate, this, containerHost, containerHost.getId() );
                    boolean trustedRelation =
                            relationManager.getRelationInfoManager().allHasReadPermissions( relationMeta );

                    if ( !trustedRelation )
                    {
                        iterator.remove();
                    }
                }
            }
        }

        for ( EnvironmentContainerHost environmentContainerHost : containerHosts )
        {
            ( ( EnvironmentContainerImpl ) environmentContainerHost ).setEnvironment( this );
        }

        return containerHosts;
    }


    public void addContainers( Set<EnvironmentContainerImpl> containers )
    {
        if ( CollectionUtil.isCollectionEmpty( containers ) )
        {
            return;
        }

        for ( EnvironmentContainerImpl container : containers )
        {
            container.setEnvironment( this );
        }

        synchronized ( this )
        {
            this.containers.addAll( containers );
        }
    }


    public synchronized void removeContainer( EnvironmentContainerHost container )
    {
        Preconditions.checkNotNull( container );

        this.containers.remove( container );
    }


    @Override
    public void destroyContainer( EnvironmentContainerHost containerHost, boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        environmentManager.destroyContainer( getId(), containerHost.getId(), async );
    }


    @Override
    public Set<Peer> getPeers() throws PeerException
    {
        Set<Peer> peers = Sets.newHashSet();

        for ( EnvironmentPeer environmentPeer : environmentPeers )
        {
            peers.add( environmentManager.resolvePeer( environmentPeer.getPeerId() ) );
        }

        return peers;
    }


    public void setEnvironmentManager( final EnvironmentManagerImpl environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof EnvironmentImpl ) )
        {
            return false;
        }

        final EnvironmentImpl that = ( EnvironmentImpl ) o;

        return getId().equals( that.getId() );
    }


    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }


    //networking settings


    @Override
    public String getSubnetCidr()
    {
        return subnetCidr;
    }


    @Override
    public void setSubnetCidr( final String cidr )
    {
        new SubnetUtils( cidr );

        this.subnetCidr = cidr;
    }


    @Override
    public Long getVni()
    {
        return vni;
    }


    public void setVni( long vni )
    {
        this.vni = vni;
    }


    @Override
    public String getP2pSubnet()
    {
        return p2pSubnet;
    }


    public void setP2PSubnet( final String p2pSubnet )
    {
        this.p2pSubnet = p2pSubnet;
    }


    @Override
    public P2pIps getP2pIps()
    {
        P2pIps result = new P2pIps();
        for ( EnvironmentPeer environmentPeer : getEnvironmentPeers() )
        {
            result.addP2pIps( environmentPeer.getRhP2pIps() );
        }
        return result;
    }


    @Override
    public boolean isMember( final Peer peer )
    {
        for ( EnvironmentPeer f : environmentPeers )
        {
            if ( f.getPeerId().equals( peer.getId() ) )
            {
                return true;
            }
        }

        return false;
    }


    @Override
    public String getP2PHash()
    {
        return P2PUtil.generateHash( environmentId );
    }


    public String getP2pKey()
    {
        return p2pKey;
    }


    public void setP2pKey( final String p2pKey )
    {
        this.p2pKey = p2pKey;
    }


    @Override
    public EnvironmentId getEnvironmentId()
    {
        if ( envId == null )
        {
            envId = new EnvironmentId( environmentId );
        }
        return envId;
    }


    @Override
    public Set<EnvironmentAlertHandler> getAlertHandlers()
    {
        return alertHandlers;
    }


    public void addAlertHandler( EnvironmentAlertHandler environmentAlertHandler )
    {
        Preconditions.checkNotNull( environmentAlertHandler, "Invalid alert handler id." );

        EnvironmentAlertHandlerImpl handlerId =
                new EnvironmentAlertHandlerImpl( environmentAlertHandler.getAlertHandlerId(),
                        environmentAlertHandler.getAlertHandlerPriority() );
        handlerId.setEnvironment( this );
        alertHandlers.add( handlerId );
    }

    public void removeAlertHandler( EnvironmentAlertHandler environmentAlertHandler )
    {
        alertHandlers.remove( environmentAlertHandler );
    }


    @Override
    public String toString()
    {
        return "EnvironmentImpl{" + "environmentId='" + environmentId + '\'' + ", peerId='" + peerId + '\'' + ", name='"
                + name + '\'' + ", creationTimestamp=" + creationTimestamp + ", subnetCidr='" + subnetCidr + '\''
                + ", vni=" + vni + ", tunnelNetwork='" + p2pSubnet + '\'' + ", containers=" + containers
                + ", peerConfs=" + environmentPeers + ", status=" + status + ", sshKeys='" + sshKeys + '\''
                + ", userId=" + userId + ", alertHandlers=" + alertHandlers + ", envId=" + envId + '}';
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
        return getEnvironmentId().getId();
    }
}
