package io.subutai.core.registration.rest.transitional;


import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInterface;
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
    private Set<HostInterfaceJson> interfaces = Sets.newHashSet();
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
    private Set<ContainerInfoJson> hostInfos = Sets.newHashSet();


    public RequestedHostJson( RequestedHost requestedHost )
    {
        this.id = requestedHost.getId();
        this.hostname = requestedHost.getHostname();
        this.secret = requestedHost.getSecret();
        this.publicKey = requestedHost.getPublicKey();
        this.cert = requestedHost.getCert();
        this.arch = requestedHost.getArch();
        this.status = requestedHost.getStatus();

        for ( HostInterface hostInterface : requestedHost.getInterfaces() )
        {
            this.interfaces.add( new HostInterfaceJson( hostInterface ) );
        }

        for ( ContainerInfo containerInfo : requestedHost.getHostInfos() )
        {
            this.hostInfos.add( new ContainerInfoJson( containerInfo ) );
        }
    }


    public String getId()
    {
        return id;
    }


    public String getHostname()
    {
        return hostname;
    }


    @Override
    public String getCert()
    {
        return cert;
    }


    public Set<HostInterface> getInterfaces()
    {
        Set<HostInterface> temp = Sets.newHashSet();
        temp.addAll( interfaces );
        return temp;
    }


    @Override
    public Set<ContainerInfo> getHostInfos()
    {
        Set<ContainerInfo> result = Sets.newHashSet();
        result.addAll( hostInfos );
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


    @Override
    public ResourceHostRegistrationStatus getStatus()
    {
        return status;
    }


    public String getSecret()
    {
        return secret;
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
        return "RequestedHostJson{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", interfaces=" + interfaces +
                ", hostInfos=" + hostInfos +
                ", arch=" + arch +
                ", secret='" + secret + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", status=" + status +
                ", hostInfos=" + hostInfos +
                ", cert=" + cert +
                '}';
    }
}
