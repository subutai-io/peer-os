package org.safehaus.subutai.core.environment.api.helper;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * Created by bahadyr on 6/24/14.
 */
public class Environment
{

    private UUID uuid;
    private Set<Node> nodes;
    private String name;
    private Set<String> containers;
    private String peerUuid;


    public Environment( String name, String peerUuid )
    {
        this.uuid = UUID.randomUUID();
        this.nodes = new HashSet<>();
        this.name = name;
        this.containers = new HashSet<>();
        this.peerUuid = peerUuid;
    }


    public void addContainer( String containerUuid )
    {
        this.containers.add( containerUuid );
    }


    public Set<String> getContainers()
    {
        return containers;
    }


    public void setContainers( final Set<String> containers )
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
