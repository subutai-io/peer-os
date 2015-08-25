package io.subutai.core.registration.rest;


import java.util.Set;

import com.google.common.collect.Sets;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.Interface;
import io.subutai.common.peer.InterfaceModel;
import io.subutai.core.registration.api.RegistrationStatus;
import io.subutai.core.registration.api.resource.host.RequestedHost;
import io.subutai.core.registration.api.resource.host.VirtualHost;


/**
 * Created by talas on 8/25/15.
 */
public class HostRequest implements RequestedHost
{
    private String id;
    private String hostname;
    private Set<InterfaceModel> interfaces = Sets.newHashSet();
    private HostArchitecture arch;
    private String secret;

    private String publicKey;
    private String restHook;
    private RegistrationStatus status;


    public HostRequest()
    {
    }


    public HostRequest( final String id, final String hostname, final HostArchitecture arch, final String publicKey,
                        final String restHook, final RegistrationStatus status )
    {
        this.id = id;
        this.hostname = hostname;
        this.arch = arch;
        this.publicKey = publicKey;
        this.restHook = restHook;
        this.status = status;
    }


    public String getId()
    {
        return id;
    }


    public String getHostname()
    {
        return hostname;
    }


    public Set<Interface> getInterfaces()
    {
        Set<Interface> temp = Sets.newHashSet();
        temp.addAll( interfaces );
        return temp;
        //        return Sets.newHashSet();
    }


    public void setInterfaces( final Set<InterfaceModel> interfaces )
    {
        this.interfaces = interfaces;
    }


    @Override
    public Set<VirtualHost> getContainers()
    {
        return Sets.newHashSet();
    }


    public void setContainers( final Set<VirtualHost> containers )
    {
        //        this.containers = containers;
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
        if ( !( o instanceof HostRequest ) )
        {
            return false;
        }

        final HostRequest that = ( HostRequest ) o;

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
        return "HostRequest{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", interfaces=" + interfaces +
                //                ", containers=" + containers +
                ", arch=" + arch +
                ", publicKey='" + publicKey + '\'' +
                ", restHook='" + restHook + '\'' +
                ", status=" + status +
                '}';
    }
}
