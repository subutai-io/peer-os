package io.subutai.common.peer;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.settings.ChannelSettings;


/**
 * Holds info about peer
 */
public class PeerInfo implements Serializable
{
    @JsonProperty
    private String id;

    @JsonProperty
    private String ownerId;

    @JsonProperty
    private String ip = "127.0.0.1";

//    @JsonProperty
//    private String gatewayIp;
//
    @JsonProperty
    private String keyPhrase = "";

    @JsonProperty
    private PeerStatus status;

    @JsonProperty
    private PeerPolicy grantedPolicy;

    @JsonProperty
    private String name;

    @JsonProperty
    private int port = Integer.valueOf( ChannelSettings.SECURE_PORT_X2 );

//    @JsonProperty
//    private int lastUsedVlanId = 100;

//    @JsonProperty
//    private String keyId;


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

//
//    public String getGatewayIp()
//    {
//        return gatewayIp;
//    }
//
//
//    public void setGatewayIp( String gatewayIp )
//    {
//        this.gatewayIp = gatewayIp;
//    }


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

//
//    public int getLastUsedVlanId()
//    {
//        return lastUsedVlanId;
//    }
//
//
//    public void setLastUsedVlanId( int lastUsedVlanId )
//    {
//        this.lastUsedVlanId = lastUsedVlanId;
//    }


//    public String getKeyId()
//    {
//        return keyId;
//    }
//
//
//    public void setKeyId( final String keyId )
//    {
//        this.keyId = keyId;
//    }
//

    public String getKeyPhrase()
    {
        return keyPhrase;
    }


    public void setKeyPhrase( final String keyPhrase )
    {
        this.keyPhrase = keyPhrase;
    }


    public PeerPolicy getGrantedPolicy()
    {
        return grantedPolicy;
    }


    public void setGrantedPolicy( final PeerPolicy grantedPolicy )
    {
        this.grantedPolicy = grantedPolicy;
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


    //    public PeerPolicy getPeerPolicy( final String remotePeerId )
    //    {
    //        if ( peerPolicies == null )
    //        {
    //            return null;
    //        }
    //        for ( PeerPolicy peerPolicy : peerPolicies )
    //        {
    //            if ( peerPolicy.getPeerId().compareTo( remotePeerId ) == 0 )
    //            {
    //                return peerPolicy;
    //            }
    //        }
    //        return null;
    //    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "PeerInfo{" );
        sb.append( "ip='" ).append( ip ).append( '\'' );
//        sb.append( ", gatewayIp='" ).append( gatewayIp ).append( '\'' );
        sb.append( ", status=" ).append( status );
        //        sb.append( ", peerPolicies=" ).append( peerPolicies );
        sb.append( ", name='" ).append( name ).append( '\'' );
        sb.append( ", id=" ).append( id );
        sb.append( ", ownerId=" ).append( ownerId );
        sb.append( ", port=" ).append( port );
//        sb.append( ", lastUsedVlanId=" ).append( lastUsedVlanId );
//        sb.append( ", keyId='" ).append( keyId ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}

