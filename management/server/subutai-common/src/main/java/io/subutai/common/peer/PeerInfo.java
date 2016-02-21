package io.subutai.common.peer;


import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.settings.SystemSettings;


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
    private String publicUrl = "";

    @JsonProperty
    private String ip = "UNKNOWN";

    //    @JsonProperty
    //    private String gatewayIp;
    //
    //    @JsonProperty
    //    private String keyPhrase = "";

    //    @JsonProperty
    //    private PeerStatus status;

    //    @JsonProperty
    //    private PeerPolicy grantedPolicy;

    @JsonProperty
    private String name;

    @JsonProperty
    private int port = SystemSettings.getSecurePortX2();

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
        return String.format( "Peer %s on %s ", id, ip );
    }


    public void setName( final String name )
    {
        this.name = name;
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


    public String getPublicUrl()
    {
        return publicUrl;
    }


    public void setPublicUrl( final String publicUrl )
    {
        try
        {
            URL url = new URL( publicUrl );
            this.ip = url.getHost();
            this.publicUrl = publicUrl;
        }
        catch ( MalformedURLException e )
        {
            // assume this is IP or domain name
            final String u = String.format( "https://%s:%s/", publicUrl, SystemSettings.getSecurePortX1() );
            try
            {
                URL url = new URL( u );
                this.ip = url.getHost();
                this.publicUrl = u;
            }
            catch ( MalformedURLException e1 )
            {
                throw new IllegalArgumentException( "Invalid public URL." );
            }
        }
    }


    public int getPort()
    {
        return port;
    }


    public void setPort( final int port )
    {
        this.port = port;
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


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "PeerInfo{" );
        sb.append( "ip='" ).append( ip ).append( '\'' );
        sb.append( ", name='" ).append( name ).append( '\'' );
        sb.append( ", id=" ).append( id );
        sb.append( ", ownerId=" ).append( ownerId );
        sb.append( '}' );
        return sb.toString();
    }
}

