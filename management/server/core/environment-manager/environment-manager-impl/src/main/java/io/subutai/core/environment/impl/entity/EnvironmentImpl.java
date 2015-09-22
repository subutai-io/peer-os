package io.subutai.core.environment.impl.entity;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
import javax.persistence.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.N2NUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.impl.dao.EnvironmentDataService;
import io.subutai.core.peer.api.LocalPeer;


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
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentImpl.class );

    @Id
    @Column( name = "environment_id" )
    private String environmentId;

    @Version
    private Long version;

    @Column( name = "peer_id", nullable = false )
    private String peerId;

    @Column( name = "name", nullable = false )
    private String name;

    @Column( name = "create_time", nullable = false )
    private long creationTimestamp = System.currentTimeMillis();

    @Column( name = "subnet_cidr" )
    private String subnetCidr;

    @Column( name = "last_used_ip_idx" )
    private int lastUsedIpIndex;

    @Column( name = "vni" )
    private Long vni;

    @Column( name = "super_node" )
    private String superNode;

    @Column( name = "super_node_port" )
    private int superNodePort;


    @Column( name = "tunnel_network" )
    private String tunnelNetwork;


    @OneToMany( mappedBy = "environment", fetch = FetchType.EAGER, targetEntity = EnvironmentContainerImpl.class,
            cascade = CascadeType.ALL, orphanRemoval = false )
    private Set<ContainerHost> containers = Sets.newHashSet();

    @OneToMany( mappedBy = "environment", fetch = FetchType.EAGER, targetEntity = PeerConfImpl.class,
            cascade = CascadeType.ALL, orphanRemoval = false )
    private Set<PeerConf> peerConfs = Sets.newHashSet();

    @Enumerated( EnumType.STRING )
    @Column( name = "status", nullable = false )
    private EnvironmentStatus status = EnvironmentStatus.EMPTY;

    @Column( name = "public_key", length = 3000 )
    private String publicKey;

    @Column( name = "user_id" )
    private Long userId;

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
        //        dataService.update( this );
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
    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public Long getVersion()
    {
        return version;
    }


    public void setVersion( final Long version )
    {
        this.version = version;
    }


    @Override
    public ContainerHost getContainerHostById( String id ) throws ContainerHostNotFoundException
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
    public Set<ContainerHost> getContainerHostsByIds( Set<String> ids ) throws ContainerHostNotFoundException
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( ids ), "Invalid id set" );

        Set<ContainerHost> hosts = Sets.newHashSet();
        for ( String id : ids )
        {
            hosts.add( getContainerHostById( id ) );
        }
        return hosts;
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
    public String getId()
    {
        return environmentId;
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
        environmentManager.destroyContainer( getId(), containerHost.getId(), async, false );
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

        for ( PeerConf peerConf : peerConfs )
        {
            peers.add( environmentManager.resolvePeer( peerConf.getPeerId() ) );
        }

        return peers;
    }


    public void removeContainer( String containerId )
    {
        Preconditions.checkNotNull( containerId );

        try
        {
            ContainerHost container = getContainerHostById( containerId );

            synchronized ( containers )
            {
                containers.remove( container );
            }
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
    }


    public void setStatus( EnvironmentStatus status )
    {
        Preconditions.checkNotNull( status );

        this.status = status;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    protected void setEnvironmentId( String environmentId )
    {
        Preconditions.checkNotNull( environmentId );

        this.environmentId = environmentId;
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
    }


    public int getLastUsedIpIndex()
    {
        return lastUsedIpIndex;
    }


    public void setLastUsedIpIndex( int lastUsedIpIndex )
    {
        this.lastUsedIpIndex = lastUsedIpIndex;
    }


    @Override
    public String getSuperNode()
    {
        return superNode;
    }


    public void setSuperNode( final String superNode )
    {
        this.superNode = superNode;
    }


    @Override
    public int getSuperNodePort()
    {
        return superNodePort;
    }


    public void setSuperNodePort( final int superNodePort )
    {
        this.superNodePort = superNodePort;
    }


    @Override
    public String getTunnelNetwork()
    {
        return tunnelNetwork;
    }


    public void setTunnelNetwork( final String tunnelNetwork )
    {
        this.tunnelNetwork = tunnelNetwork;
    }


    @Override
    public Map<String, String> getTunnels()
    {
        Map<String, String> result = new HashMap<>();
        for ( PeerConf peerConf : getPeerConfs() )
        {
            result.put( peerConf.getPeerId(), peerConf.getTunnelAddress() );
        }
        return result;
    }


    @Override
    public boolean isMember( final Peer peer )
    {
        boolean found = false;
        for ( Iterator<PeerConf> it = peerConfs.iterator(); it.hasNext(); )
        {
            PeerConf f = it.next();
            if ( f.getPeerId().equals( peer.getId() ) )
            {
                found = true;
                break;
            }
        }
        return found;
    }


    @Override
    public String getTunnelInterfaceName()
    {
        if ( tunnelNetwork == null )
        {
            throw new IllegalStateException( "Tunnel network not defined yet." );
        }
        return N2NUtil.generateInterfaceName( tunnelNetwork );
    }


    @Override
    public String getTunnelCommunityName()
    {
        if ( tunnelNetwork == null )
        {
            throw new IllegalStateException( "Tunnel network not defined yet." );
        }
        return N2NUtil.generateCommunityName( tunnelNetwork );
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "EnvironmentImpl{" );
        sb.append( "userId=" ).append( userId );
        sb.append( ", publicKey='" ).append( publicKey ).append( '\'' );
        sb.append( ", status=" ).append( status );
        sb.append( ", peerConfs=" ).append( peerConfs );
        sb.append( ", containers=" ).append( containers );
        sb.append( ", superNodePort=" ).append( superNodePort );
        sb.append( ", superNode='" ).append( superNode ).append( '\'' );
        sb.append( ", vni=" ).append( vni );
        sb.append( ", lastUsedIpIndex=" ).append( lastUsedIpIndex );
        sb.append( ", subnetCidr='" ).append( subnetCidr ).append( '\'' );
        sb.append( ", creationTimestamp=" ).append( creationTimestamp );
        sb.append( ", name='" ).append( name ).append( '\'' );
        sb.append( ", peerId='" ).append( peerId ).append( '\'' );
        sb.append( ", environmentId='" ).append( environmentId ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}
