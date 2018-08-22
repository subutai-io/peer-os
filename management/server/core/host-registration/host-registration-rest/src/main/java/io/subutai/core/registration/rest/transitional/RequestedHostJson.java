package io.subutai.core.registration.rest.transitional;


import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;
import io.subutai.core.registration.api.service.ContainerInfo;
import io.subutai.core.registration.api.service.RequestedHost;


public class RequestedHostJson implements RequestedHost
{
    @Expose
    private String id;
    @Expose
    private String hostname;
    @Expose
    private HostArchitecture arch;
    @Expose
    private String secret;
    @Expose
    private String publicKey;
    @Expose
    private ResourceHostRegistrationStatus status;
    @Expose
    private String cert;
    @Expose
    private boolean isManagement = false;
    @Expose
    private boolean isConnected = false;
    @Expose
    private Set<ContainerInfoJson> hostInfos = Sets.newHashSet();
    @Expose
    @SerializedName( value = "ip", alternate = { "address" } )
    private String ip;
    @Expose
    private String version;


    public RequestedHostJson( RequestedHost requestedHost )
    {
        this.id = requestedHost.getId();
        this.hostname = requestedHost.getHostname();
        this.secret = requestedHost.getSecret();
        this.publicKey = requestedHost.getPublicKey();
        this.cert = requestedHost.getCert();
        this.arch = requestedHost.getArch();
        this.status = requestedHost.getStatus();

        for ( ContainerInfo containerInfo : requestedHost.getHostInfos() )
        {
            this.hostInfos.add( new ContainerInfoJson( containerInfo ) );
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
    public String getCert()
    {
        return cert;
    }


    @Override
    public Set<ContainerInfo> getHostInfos()
    {
        Set<ContainerInfo> result = Sets.newHashSet();
        if ( !CollectionUtil.isCollectionEmpty( hostInfos ) )
        {
            result.addAll( hostInfos );
        }
        return result;
    }


    @Override
    public HostArchitecture getArch()
    {
        return arch;
    }


    @Override
    public String getPublicKey()
    {
        return publicKey;
    }


    public void setManagement( final boolean isManagement )
    {
        this.isManagement = isManagement;
    }


    public void setConnected( final boolean connected )
    {
        isConnected = connected;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    @Override
    public ResourceHostRegistrationStatus getStatus()
    {
        return status;
    }


    @Override
    public String getSecret()
    {
        return secret;
    }


    public void setVersion( final String rhVersion )
    {
        this.version = rhVersion;
    }


    @Override
    public String getAddress()
    {
        return ip;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof RequestedHostJson ) )
        {
            return false;
        }

        final RequestedHostJson that = ( RequestedHostJson ) o;

        return !( id != null ? !id.equals( that.id ) : that.id != null );
    }


    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }


    @Override
    public String toString()
    {
        return "RequestedHostJson{" + "id='" + id + '\'' + ", hostname='" + hostname + '\'' + ", hostInfos=" + hostInfos
                + ", arch=" + arch + ", secret='" + secret + '\'' + ", publicKey='" + publicKey + '\'' + ", status="
                + status + ", connected=" + isConnected + ", hostInfos=" + hostInfos + ", cert=" + cert + '}';
    }
}
