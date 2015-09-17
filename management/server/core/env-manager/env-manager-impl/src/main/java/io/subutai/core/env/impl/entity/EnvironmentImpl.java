package io.subutai.core.env.impl.entity;


import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.env.impl.dao.EnvironmentDataService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Database entity to store environment specific fields.
 *
 * @see EnvironmentContainerImpl
 * @see io.subutai.common.peer.ContainerHost
 */
@Entity
@Table( name = "environment" )
@Access( AccessType.FIELD )
public class EnvironmentImpl implements Environment, Serializable
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentImpl.class.getName() );

    @Id
    @Column( name = "environment_id" )
    private String environmentId;

    @Column( name = "name" )
    private String name;

    @Column( name = "create_time" )
    private long creationTimestamp;

    @Column( name = "subnet_cidr" )
    private String subnetCidr;

    @Column( name = "last_used_ip_idx" )
    private int lastUsedIpIndex;

    @Column( name = "vni" )
    private Long vni;

    @OneToMany( mappedBy = "environment", fetch = FetchType.EAGER, targetEntity = EnvironmentContainerImpl.class,
            cascade = CascadeType.ALL, orphanRemoval = true )
    private Set<ContainerHost> containers = Sets.newHashSet();

    @OneToMany( mappedBy = "environment", fetch = FetchType.EAGER, targetEntity = PeerConfImpl.class,
            cascade = CascadeType.ALL, orphanRemoval = false )
    private Set<PeerConf> peerConfs = Sets.newHashSet();

    @Enumerated( EnumType.STRING )
    private EnvironmentStatus status;

    @Column( name = "public_key", length = 3000 )
    private String publicKey;

    @Column( name = "user_id" )
    private Long userId;


    @Transient
    private EnvironmentDataService dataService;
    @Transient
    private EnvironmentManager environmentManager;


    protected EnvironmentImpl()
    {
    }


    public EnvironmentImpl( String name, String subnetCidr, String sshKey, Long userId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ) );
        SubnetUtils cidr = new SubnetUtils( subnetCidr );

        this.name = name;
        this.subnetCidr = cidr.getInfo().getCidrSignature();
        this.publicKey = Strings.isNullOrEmpty( sshKey ) ? null : sshKey.trim();
        this.environmentId = UUID.randomUUID().toString();
        this.creationTimestamp = System.currentTimeMillis();
        this.status = EnvironmentStatus.EMPTY;
        this.lastUsedIpIndex = 0;//0 is reserved for gateway
        this.userId = userId;
    }


    @Override
    public Set<PeerConf> getPeerConfs()
    {
        return peerConfs;
    }


    public void setPeerConfs( final Set<PeerConf> peerConfs )
    {
        this.peerConfs = peerConfs;
    }


    @Override
    public String getSshKey()
    {
        return publicKey;
    }


    @Override
    public void setSshKey( final String sshKey, boolean async ) throws EnvironmentModificationException
    {
        try
        {
            environmentManager.setSshKey( getId(), sshKey, async );
        }
        catch ( EnvironmentNotFoundException e )
        {
            //this should not happen
            LOG.error( String.format( "Error setting ssh key to environment %s", getName() ), e );
            throw new EnvironmentModificationException( e );
        }
    }


    public void saveSshKey( final String sshKey )
    {
        this.publicKey = Strings.isNullOrEmpty( sshKey ) ? null : sshKey.trim();
        dataService.update( this );
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


    @Override
    public EnvironmentStatus getStatus()
    {
        return status;
    }


    @Override
    public ContainerHost getContainerHostById( UUID id ) throws ContainerHostNotFoundException
    {
        Preconditions.checkNotNull( id, "Invalid id" );

        for ( final ContainerHost containerHost : getContainerHosts() )
        {
            if ( containerHost.getId().equals( id ) )
            {
                return containerHost;
            }
        }
        throw new ContainerHostNotFoundException( String.format( "Container host not found by id %s", id ) );
    }


    @Override
    public ContainerHost getContainerHostByHostname( String hostname ) throws ContainerHostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );

        for ( final ContainerHost containerHost : getContainerHosts() )
        {
            if ( containerHost.getHostname().equalsIgnoreCase( hostname ) )
            {
                return containerHost;
            }
        }
        throw new ContainerHostNotFoundException( String.format( "Container host not found by name %s", hostname ) );
    }


    @Override
    public Set<ContainerHost> getContainerHostsByIds( Set<UUID> ids ) throws ContainerHostNotFoundException
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( ids ), "Invalid id set" );

        Set<ContainerHost> hosts = Sets.newHashSet();
        for ( UUID id : ids )
        {
            hosts.add( getContainerHostById( id ) );
        }
        return hosts;
    }


    @Override
    public String findN2nIp( final String peerId )
    {
        String result = null;
        LOG.debug( "Finding n2n ip for " + peerId );
        for ( PeerConf p : peerConfs )
        {
            LOG.debug( String.format( "%s %s", p.getN2NConfig().getPeerId(), p.getN2NConfig().getAddress() ) );
            if ( p.getN2NConfig().getPeerId().toString().equals( peerId ) )
            {
                result = p.getN2NConfig().getAddress();
                break;
            }
        }

        LOG.debug( "N2N ip for " + peerId + ":" + result );

        return result;
    }


    @Override
    public void addEnvironmentPeer( final PeerConf peerConf )
    {
        if ( peerConf == null )
        {
            throw new IllegalArgumentException( "Environment peer could not be null." );
        }

        peerConf.setEnvironment( this );
        peerConfs.add( peerConf );
    }


    @Override
    public UUID getId()
    {
        return UUID.fromString( environmentId );
    }


    @Override
    public Set<ContainerHost> getContainerHosts()
    {
        synchronized ( containers )
        {
            return Sets.newConcurrentHashSet( containers );
        }
    }


    @Override
    public void destroyContainer( ContainerHost containerHost, boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        environmentManager.destroyContainer( containerHost, async, false );
    }


    @Override
    public Set<ContainerHost> growEnvironment( final Topology topology, boolean async )
            throws EnvironmentModificationException
    {
        try
        {
            return environmentManager.growEnvironment( getId(), topology, async );
        }
        catch ( EnvironmentNotFoundException e )
        {
            //this should not happen
            LOG.error( String.format( "Error growing environment %s", getName() ), e );
            throw new EnvironmentModificationException( e );
        }
    }


    @Override
    public Set<Peer> getPeers()
    {
        Set<Peer> peers = Sets.newHashSet();

        for ( ContainerHost containerHost : getContainerHosts() )
        {
            peers.add( containerHost.getPeer() );
        }

        return peers;
    }


    public void removeContainer( UUID containerId )
    {
        Preconditions.checkNotNull( containerId );

        try
        {
            ContainerHost container = getContainerHostById( containerId );

            synchronized ( containers )
            {
                containers.remove( container );
            }

            dataService.update( this );
        }
        catch ( ContainerHostNotFoundException e )
        {
            LOG.warn( String.format( "Failed to remove container %s because it does not exist", containerId ), e );
        }
    }


    public void addContainers( Set<EnvironmentContainerImpl> containers )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containers ) );

        for ( EnvironmentContainerImpl container : containers )
        {
            container.setEnvironment( this );
        }

        synchronized ( this.containers )
        {
            this.containers.addAll( containers );
        }

        dataService.update( this );
    }


    public void setStatus( EnvironmentStatus status )
    {
        Preconditions.checkNotNull( status );

        this.status = status;

        dataService.update( this );
    }


    public void setDataService( final EnvironmentDataService dataService )
    {
        Preconditions.checkNotNull( dataService );

        this.dataService = dataService;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    protected void setEnvironmentId( UUID environmentId )
    {
        Preconditions.checkNotNull( environmentId );

        this.environmentId = environmentId.toString();
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
    public Long getVni()
    {
        return vni;
    }


    public void setVni( long vni )
    {
        this.vni = vni;

        dataService.update( this );
    }


    public int getLastUsedIpIndex()
    {
        return lastUsedIpIndex;
    }


    public void setLastUsedIpIndex( int lastUsedIpIndex )
    {
        this.lastUsedIpIndex = lastUsedIpIndex;

        dataService.update( this );
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "environmentId", environmentId ).add( "name", name )
                      .add( "creationTimestamp", creationTimestamp ).add( "status", status )
                      .add( "containers", getContainerHosts() ).toString();
    }
}
