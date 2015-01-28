package org.safehaus.subutai.core.env.impl.entity;


import java.io.Serializable;
import java.util.Collections;
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

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.env.api.Environment;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.api.EnvironmentStatus;
import org.safehaus.subutai.core.env.api.build.Topology;
import org.safehaus.subutai.core.env.api.exception.ContainerHostNotFoundException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentModificationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;


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

    @Column( name = "peer_vlan_info" )
    private String peerVlanInfo;

    @Column( name = "vni" )
    private int vni;

    @OneToMany( mappedBy = "environment", fetch = FetchType.EAGER, targetEntity = EnvironmentContainerImpl.class,
            cascade = CascadeType.ALL, orphanRemoval = true )
    private Set<ContainerHost> containers = Sets.newHashSet();

    @Enumerated( EnumType.STRING )
    private EnvironmentStatus status = EnvironmentStatus.UNDER_MODIFICATION;

    @Column( name = "public_key", length = 3000 )
    private String publicKey;

    @Transient
    private DataService dataService;
    @Transient
    private EnvironmentManager environmentManager;


    protected EnvironmentImpl()
    {
    }


    public EnvironmentImpl( String name )
    {
        this.name = name;
        this.environmentId = UUID.randomUUID().toString();
        this.creationTimestamp = System.currentTimeMillis();
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


    public void saveSshKey( final String publicKey )
    {
        this.publicKey = publicKey;
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
    public UUID getId()
    {
        return UUID.fromString( environmentId );
    }


    @Override
    public Set<ContainerHost> getContainerHosts()
    {
        return containers == null ? Sets.<ContainerHost>newHashSet() : Collections.unmodifiableSet( containers );
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


    public void removeContainer( UUID containerId )
    {
        Preconditions.checkNotNull( containerId );

        try
        {
            ContainerHost container = getContainerHostById( containerId );

            containers.remove( container );

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

        this.containers.addAll( containers );

        dataService.update( this );
    }


    public void setStatus( EnvironmentStatus status )
    {
        Preconditions.checkNotNull( status );

        this.status = status;

        dataService.update( this );
    }


    public void setDataService( final DataService dataService )
    {
        this.dataService = dataService;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
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

        if ( environmentId != null ? !environmentId.equals( that.environmentId ) : that.environmentId != null )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return environmentId != null ? environmentId.hashCode() : 0;
    }


    //networking settings


    @Override
    public String getSubnetCidr()
    {
        return subnetCidr;
    }


    @Override
    public Map<UUID, Integer> getPeerVlanInfo()
    {
        Map<UUID, Integer> map = deserializePeerVlanInfo();
        return Collections.unmodifiableMap( map );
    }


    public void setPeerVlanInfo( UUID peerId, int vlanId )
    {
        Preconditions.checkNotNull( peerId );

        Map<UUID, Integer> map = deserializePeerVlanInfo();
        map.put( peerId, vlanId );
        this.peerVlanInfo = JsonUtil.to( map );
    }


    @Override
    public int getVni()
    {
        return vni;
    }


    public void setVni( int vni )
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


    public void setSubnetCidr( String subnetCidr )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subnetCidr ) );

        // this ctor checks CIDR notation format validity
        SubnetUtils cidr = new SubnetUtils( subnetCidr );
        this.subnetCidr = cidr.getInfo().getCidrSignature();
    }


    private Map<UUID, Integer> deserializePeerVlanInfo()
    {
        if ( Strings.isNullOrEmpty( peerVlanInfo ) )
        {
            return Maps.newHashMap();
        }
        TypeToken<Map<UUID, Integer>> typeToken = new TypeToken<Map<UUID, Integer>>()
        {};
        return JsonUtil.fromJson( peerVlanInfo, typeToken.getType() );
    }
}
