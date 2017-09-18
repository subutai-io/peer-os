package io.subutai.common.host;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;


public class HostInfoModel implements HostInfo
{
    @SerializedName( "id" )
    @JsonProperty( "id" )
    protected String id;
    @SerializedName( "hostname" )
    @JsonProperty( "hostname" )
    protected String hostname;
    @SerializedName( "arch" )
    @JsonProperty( "arch" )
    protected HostArchitecture hostArchitecture;


    public HostInfoModel( @JsonProperty( "id" ) final String id, @JsonProperty( "hostname" ) final String hostname,
                          @JsonProperty( "arch" ) final HostArchitecture hostArchitecture )
    {
        this.id = id;
        this.hostname = hostname;
        this.hostArchitecture = hostArchitecture;
    }


    public HostInfoModel( HostInfo hostInfo )
    {
        this.id = hostInfo.getId();
        this.hostname = hostInfo.getHostname();
        this.hostArchitecture = hostInfo.getArch();
        if ( hostArchitecture == null )
        {
            hostArchitecture = HostArchitecture.AMD64;
        }
    }


    @Override
    public String getId()
    {
        return id;
    }


    @Override
    public String getHostname()
    {
        return hostname;
    }


    @Override
    public HostArchitecture getArch()
    {
        return hostArchitecture;
    }


    @Override
    public int compareTo( final HostInfo o )
    {
        return hostname.compareTo( o.getHostname() );
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof HostInfoModel ) )
        {
            return false;
        }

        final HostInfoModel that = ( HostInfoModel ) o;

        return hostArchitecture == that.hostArchitecture && hostname.equals( that.hostname ) && id.equals( that.id );
    }


    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + hostname.hashCode();
        result = 31 * result + hostArchitecture.hashCode();
        return result;
    }
}
