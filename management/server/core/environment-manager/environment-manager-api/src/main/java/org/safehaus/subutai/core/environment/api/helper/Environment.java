package org.safehaus.subutai.core.environment.api.helper;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.peer.api.ContainerHost;


public class Environment
{

    private final ServiceLocator serviceLocator;
    private UUID id;
    private String name;
    private Set<ContainerHost> containers;
    private EnvironmentStatusEnum status;
    private long creationTimestamp;


    public Environment( String name )
    {
        this.name = name;
        this.id = UUIDUtil.generateTimeBasedUUID();
        this.containers = new HashSet<>();
        this.serviceLocator = new ServiceLocator();
        this.status = EnvironmentStatusEnum.EMPTY;
        this.creationTimestamp = System.currentTimeMillis();
    }


    public long getCreationTimestamp()
    {
        return creationTimestamp;
    }


    public EnvironmentStatusEnum getStatus()
    {
        return status;
    }


    public void setStatus( final EnvironmentStatusEnum status )
    {
        this.status = status;
    }


    public void addContainer( ContainerHost container )
    {
        container.setEnvironmentId( id );
        this.containers.add( container );
    }


    public Set<ContainerHost> getContainers()
    {
        return containers;
    }


    //TODO implement this method after migrating to new domain model
    public Set<ContainerHost> getContainerHosts()
    {
        return Collections.EMPTY_SET;
    }


    public void setContainers( final Set<ContainerHost> containers )
    {
        this.containers = containers;
    }


    public String getName()
    {
        return name;
    }


    public UUID getId()
    {
        return id;
    }


    public void invoke( PeerCommandMessage commandMessage )
    {
        try
        {
            EnvironmentManager environmentManager = this.serviceLocator.getServiceNoCache( EnvironmentManager.class );
            environmentManager.invoke( commandMessage );
        }
        catch ( NamingException e )
        {
            commandMessage.setProccessed( true );
            commandMessage.setExceptionMessage( e.toString() );
            //            commandMessage.setSuccess( false );
        }
    }
}
