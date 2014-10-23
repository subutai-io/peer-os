package org.safehaus.subutai.core.environment.api.helper;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;


public class Environment
{

    private final ServiceLocator serviceLocator;
    private UUID uuid;
    private String name;
    private Set<EnvironmentContainer> containers;
    private EnvironmentStatusEnum status;
    private long creationTimestamp;


    public Environment( String name )
    {
        this.name = name;
        this.uuid = UUIDUtil.generateTimeBasedUUID();
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


    public void addContainer( EnvironmentContainer container )
    {
        container.setEnvironmentId( uuid );
        this.containers.add( container );
    }


    public Set<EnvironmentContainer> getContainers()
    {
        return containers;
    }


    public void setContainers( final Set<EnvironmentContainer> containers )
    {
        this.containers = containers;
    }


    public String getName()
    {
        return name;
    }


    public UUID getUuid()
    {
        return uuid;
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
