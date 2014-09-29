package org.safehaus.subutai.core.environment.api.helper;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.environment.api.EnvironmentContainer;


/**
 * Created by bahadyr on 6/24/14.
 */
public class Environment
{

    private UUID uuid;
    private Set<Node> nodes;
    private String name;
    private Set<EnvironmentContainer> containers;


    public Environment( final String name )
    {
        this.nodes = new HashSet<>();
        this.name = name;
        this.uuid = UUID.randomUUID();
        this.containers = new HashSet<>();
    }


    public void addContainer( EnvironmentContainer containerUuid )
    {
        this.containers.add( containerUuid );
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


    @Override
    public String toString()
    {
        return "Environment{" +
                "uuid=" + uuid +
                ", nodes=" + nodes +
                ", name='" + name + '\'' +
                '}';
    }
}
