package org.safehaus.subutai.core.environment.api.helper;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentContainer;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;


public class Environment
{

    private final ServiceLocator serviceLocator;
    private UUID uuid;
    private Set<Node> nodes;
    private String name;
    private Set<EnvironmentContainer> containers;


    public Environment( String name )
    {
        this.nodes = new HashSet<>();
        this.name = name;
        this.uuid = UUIDUtil.generateTimeBasedUUID();
        this.containers = new HashSet<>();
        this.serviceLocator = new ServiceLocator();
    }


    public void addContainer( EnvironmentContainer container )
    {
        container.setEnvironment( this );
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


    public Set<Node> getNodes()
    {
        return nodes;
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
            commandMessage.setSuccess( false );
        }
    }


    @Override
    public String toString()
    {
        return "Environment{" +
                "uuid=" + uuid +
                ", nodes=" + nodes +
                ", name='" + name + '\'' +
                ", containers=" + containers +
                '}';
    }
}
