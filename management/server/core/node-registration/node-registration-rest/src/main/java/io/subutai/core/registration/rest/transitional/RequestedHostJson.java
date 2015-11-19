package io.subutai.core.registration.rest.transitional;


import java.util.Set;

import com.google.common.collect.Sets;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.Interface;
import io.subutai.core.registration.api.RegistrationStatus;
import io.subutai.core.registration.api.service.ContainerInfo;
import io.subutai.core.registration.api.service.RequestedHost;


public class RequestedHostJson implements RequestedHost
{
    private String id;
    private String hostname;
    private Set<HostInterfaceJson> interfaces = Sets.newHashSet();
    private HostArchitecture arch;
    private String secret;

    private String publicKey;
    private String restHook;
    private RegistrationStatus status;
    private String cert;

    private Set<ContainerInfoJson> hostInfos = Sets.newHashSet();


    public RequestedHostJson()
    {
        this.arch = HostArchitecture.AMD64;
    }


    public RequestedHostJson( final String id, final String hostname, final HostArchitecture arch,
                              final String publicKey, final String restHook, final RegistrationStatus status )
    {
        this.id = id;
        this.hostname = hostname;
        this.arch = arch;
        this.publicKey = publicKey;
        this.restHook = restHook;
        this.status = status;
        if ( arch == null )
        {
            this.arch = HostArchitecture.AMD64;
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


    public Set<Interface> getNetInterfaces()
    {
        Set<Interface> temp = Sets.newHashSet();
        temp.addAll( interfaces );
        return temp;
        //        return Sets.newHashSet();
    }


    public void setInterfaces( final Set<HostInterfaceJson> interfaces )
    {
        this.interfaces = interfaces;
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


    @Override
    public String getRestHook()
    {
        return restHook;
    }


    @Override
    public void setRestHook( final String restHook )
    {
        this.restHook = restHook;
    }


    @Override
    public RegistrationStatus getStatus()
    {
        return status;
    }


    public void setStatus( final RegistrationStatus status )
    {
        this.status = status;
    }


    public String getSecret()
    {
        return secret;
    }


    public void setSecret( final String secret )
    {
        this.secret = secret;
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
                ", restHook='" + restHook + '\'' +
                ", status=" + status +
                ", hostInfos=" + hostInfos +
                '}';
    }
}
