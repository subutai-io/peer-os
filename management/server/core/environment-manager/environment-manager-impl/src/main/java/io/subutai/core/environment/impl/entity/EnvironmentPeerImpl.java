package io.subutai.core.environment.impl.entity;


import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentPeer;
import io.subutai.common.environment.RhP2pIp;


@Entity
@Table( name = "env_peer" )
@Access( AccessType.FIELD )
public class EnvironmentPeerImpl implements EnvironmentPeer
{
    @Id
    @Column( name = "id" )
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long id;

    @Column( name = "peer_id", nullable = false )
    private String peerId;

    @Column( name = "vlan", nullable = false )
    private Integer vlan;

    @ElementCollection( targetClass = RhP2PIpEntity.class, fetch = FetchType.EAGER )
    @CollectionTable( name = "RH_P2P_IP", joinColumns = @JoinColumn( name = "PEER_ID" ) )
    private Set<RhP2pIp> rhP2pIps;

    @ManyToOne( targetEntity = EnvironmentImpl.class )
    @JoinColumn( name = "environment_id" )
    private Environment environment;


    public EnvironmentPeerImpl( final String peerId, final Integer vlan )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );
        Preconditions.checkNotNull( vlan );

        this.peerId = peerId;
        this.vlan = vlan;
        this.rhP2pIps = Sets.newHashSet();
    }


    public EnvironmentPeerImpl()
    {
    }


    public void addRhP2pIps( Set<RhP2pIp> rhP2pIps )
    {
        Preconditions.checkNotNull( rhP2pIps );

        for ( RhP2pIp rhP2pIp : rhP2pIps )
        {

            this.rhP2pIps.add( new RhP2PIpEntity( rhP2pIp.getRhId(), rhP2pIp.getP2pIp() ) );
        }
    }


    public Set<RhP2pIp> getRhP2pIps()
    {
        return rhP2pIps;
    }


    public Long getId()
    {
        return id;
    }


    @Override
    public Integer getVlan()
    {
        return vlan;
    }


    @Override
    public Environment getEnvironment()
    {
        return environment;
    }


    public void setEnvironment( final Environment environment )
    {
        this.environment = environment;
    }


    @Override
    public String getPeerId()
    {
        return peerId;
    }


    @Override
    public String toString()
    {
        return "PeerConfImpl{" + "id=" + id + ", peerId='" + peerId + '\'' + ", environment=" + environment.getId()
                + '}';
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof EnvironmentPeerImpl ) )
        {
            return false;
        }

        final EnvironmentPeerImpl peerConf = ( EnvironmentPeerImpl ) o;

        if ( !peerId.equals( peerConf.peerId ) )
        {
            return false;
        }
        return environment.equals( peerConf.environment );
    }


    @Override
    public int hashCode()
    {
        int result = peerId.hashCode();
        result = 31 * result + environment.hashCode();
        return result;
    }
}
