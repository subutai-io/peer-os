package io.subutai.core.environment.impl.entity;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
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
import javax.persistence.Version;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.util.CollectionUtil;


@Entity
@Table( name = "env_peer" )
@Access( AccessType.FIELD )
public class PeerConfImpl implements PeerConf, Serializable
{
    @Id
    @Column( name = "id" )
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long id;

    @Version
    private Long version;

    @Column( name = "peer_id", nullable = false )
    private String peerId;


    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    @Column( name = "p2p_ips", nullable = false )
    private Set<String> p2pIps = new HashSet<>();

    @ManyToOne( targetEntity = EnvironmentImpl.class )
    @JoinColumn( name = "environment_id" )
    private Environment environment;


    public PeerConfImpl( final String peerId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

        this.peerId = peerId;
        this.p2pIps = new HashSet<>();
    }


    public void addP2pIps( Set<String> p2pIps )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( p2pIps ) );

        this.p2pIps.addAll( p2pIps );
    }


    public Set<String> getP2pIps()
    {
        return p2pIps;
    }


    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
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
        final StringBuffer sb = new StringBuffer( "PeerConfImpl{" );
        sb.append( "id=" ).append( id );
        sb.append( ", peerId='" ).append( peerId ).append( '\'' );
        sb.append( ", environment=" ).append( environment.getId() );
        sb.append( '}' );
        return sb.toString();
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof PeerConfImpl ) )
        {
            return false;
        }

        final PeerConfImpl peerConf = ( PeerConfImpl ) o;

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
