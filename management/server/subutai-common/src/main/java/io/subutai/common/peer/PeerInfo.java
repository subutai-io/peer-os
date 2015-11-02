package io.subutai.common.peer;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.settings.ChannelSettings;


/**
 * Holds info about peer
 */
public class PeerInfo implements Serializable
{
    @JsonProperty
    private String ip = "127.0.0.1";
    private String gatewayIp;
    private String keyPhrase = "";
    @JsonProperty
    private PeerStatus status;
    private Set<PeerPolicy> peerPolicies = new HashSet<>();

    @JsonProperty
    private String name;
    @JsonProperty
    private String id;
    @JsonProperty
    private String ownerId;
    private int port = Integer.valueOf( ChannelSettings.SECURE_PORT_X2 );
    @JsonProperty
    private int lastUsedVlanId = 100;
    private String keyId;


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public void setPort( final int port )
    {
        this.port = port;
    }


    public String getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final String ownerId )
    {
        this.ownerId = ownerId;
    }


    public String getIp()
    {
        return ip;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public String getGatewayIp()
    {
        return gatewayIp;
    }


    public void setGatewayIp( String gatewayIp )
    {
        this.gatewayIp = gatewayIp;
    }


    public PeerStatus getStatus()
    {
        return status;
    }


    public void setStatus( final PeerStatus status )
    {
        this.status = status;
    }


    public int getPort()
    {
        return port;
    }


    public int getLastUsedVlanId()
    {
        return lastUsedVlanId;
    }


    public void setLastUsedVlanId( int lastUsedVlanId )
    {
        this.lastUsedVlanId = lastUsedVlanId;
    }


    public String getKeyId()
    {
        return keyId;
    }


    public void setKeyId( final String keyId )
    {
        this.keyId = keyId;
    }


    public String getKeyPhrase()
    {
        return keyPhrase;
    }


    public void setKeyPhrase( final String keyPhrase )
    {
        this.keyPhrase = keyPhrase;
    }


    public Set<PeerPolicy> getPeerPolicies()
    {
        return peerPolicies;
    }


    public void setPeerPolicies( final Set<PeerPolicy> peerPolicies )
    {
        this.peerPolicies = peerPolicies;
    }


    public void setPeerPolicy( final PeerPolicy peerPolicy )
    {
        PeerPolicy oldPeerPolicy = getPeerPolicy( peerPolicy.getRemotePeerId() );
        if ( oldPeerPolicy != null )
        {
            peerPolicies.remove( oldPeerPolicy );
        }
        peerPolicies.add( peerPolicy );
    }


    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode( this.id );
        return hash;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof PeerInfo )
        {
            PeerInfo other = ( PeerInfo ) obj;
            return Objects.equals( this.id, other.id );
        }
        return false;
    }


    public PeerPolicy getPeerPolicy( final String remotePeerId )
    {
        if ( peerPolicies == null )
        {
            return null;
        }
        for ( PeerPolicy peerPolicy : peerPolicies )
        {
            if ( peerPolicy.getRemotePeerId().compareTo( remotePeerId ) == 0 )
            {
                return peerPolicy;
            }
        }
        return null;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "PeerInfo{" );
        sb.append( "ip='" ).append( ip ).append( '\'' );
        sb.append( ", gatewayIp='" ).append( gatewayIp ).append( '\'' );
        sb.append( ", status=" ).append( status );
        sb.append( ", peerPolicies=" ).append( peerPolicies );
        sb.append( ", name='" ).append( name ).append( '\'' );
        sb.append( ", id=" ).append( id );
        sb.append( ", ownerId=" ).append( ownerId );
        sb.append( ", port=" ).append( port );
        sb.append( ", lastUsedVlanId=" ).append( lastUsedVlanId );
        sb.append( ", keyId='" ).append( keyId ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}

