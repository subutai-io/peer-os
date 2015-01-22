package org.safehaus.subutai.core.env.impl.entity;


import java.io.Serializable;
import java.util.Collections;
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

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.env.api.Environment;
import org.safehaus.subutai.core.env.api.EnvironmentStatus;
import org.safehaus.subutai.core.env.api.exception.ContainerHostNotFoundException;

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
    private EnvironmentStatus status = EnvironmentStatus.EMPTY;

    @Transient
    private DataService dataService;


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


    @Override
    public ContainerHost getContainerHostById( UUID id ) throws ContainerHostNotFoundException
    {
        Preconditions.checkNotNull( id, "Invalid id" );

        Iterator<ContainerHost> iterator = getContainerHosts().iterator();
        while ( iterator.hasNext() )
        {
            ContainerHost containerHost = iterator.next();
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

        Iterator<ContainerHost> iterator = getContainerHosts().iterator();
        while ( iterator.hasNext() )
        {
            ContainerHost containerHost = iterator.next();
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


    public void removeContainer( UUID containerId ) throws ContainerHostNotFoundException
    {
        Preconditions.checkNotNull( containerId );

        ContainerHost container = getContainerHostById( containerId );

        containers.remove( container );

        dataService.update( this );
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

        dataService.update( this );
    }
}
