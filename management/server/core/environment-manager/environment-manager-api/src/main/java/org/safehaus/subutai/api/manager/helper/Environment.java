package org.safehaus.subutai.api.manager.helper;


import java.util.Set;
import java.util.UUID;


/**
 * Created by bahadyr on 6/24/14.
 */
public class Environment  {

    private String owner;
    private UUID uuid;
    //user set<Node> instead of Set<Agents>
    private Set<Node> nodes;
    private String name;


        public String getName() {
            return name;
        }


        public void setName( final String name ) {
            this.name = name;
        }


    public Environment() {
        this.uuid = UUID.randomUUID();
    }


    public Set<Node> getNodes() {
        return nodes;
    }


    public UUID getUuid() {
        return uuid;
    }


    public void setUuid( final UUID uuid ) {
        this.uuid = uuid;
    }


    public void setNodes( final Set<Node> nodes ) {
        this.nodes = nodes;
    }


    @Override
    public String toString() {
        return "Environment{" +
                "owner='" + owner + '\'' +
                ", uuid=" + uuid +
                '}';
    }
}
