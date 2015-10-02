package io.subutai.core.environment.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.protocol.N2NConfig;


@Entity
@Table( name = "environment_peer" )
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

    @Column( name = "tunnel_address", nullable = false )
    private String tunnelAddress;

    @ManyToOne( targetEntity = EnvironmentImpl.class )
    @JoinColumn( name = "environment_id" )
    private Environment environment;


    public PeerConfImpl( final N2NConfig config )
    {
        this.peerId = config.getPeerId();
        this.tunnelAddress = config.getAddress();
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


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    @Override
    public String getTunnelAddress()
    {
        return tunnelAddress;
    }


    public void setTunnelAddress( final String tunnelAddress )
    {
        this.tunnelAddress = tunnelAddress;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "PeerConfImpl{" );
        sb.append( "id=" ).append( id );
        sb.append( ", peerId='" ).append( peerId ).append( '\'' );
        sb.append( ", tunnelAddress='" ).append( tunnelAddress ).append( '\'' );
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
        if ( !tunnelAddress.equals( peerConf.tunnelAddress ) )
        {
            return false;
        }
        return environment.equals( peerConf.environment );
    }


    @Override
    public int hashCode()
    {
        int result = peerId.hashCode();
        result = 31 * result + tunnelAddress.hashCode();
        result = 31 * result + environment.hashCode();
        return result;
    }
}
