package org.safehaus.subutai.core.environment.api.helper;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * Created by bahadyr on 6/24/14.
 */
public class Environment {

    private UUID uuid;
    private Set<Node> nodes;
    private String name;


    public Environment(String name) {
        this.uuid = UUID.randomUUID();
        this.nodes = new HashSet<>();
        this.name = name;
    }


    public String getName() {
        return name;
    }


    public Set<Node> getNodes() {
        return nodes;
    }


    public UUID getUuid() {
        return uuid;
    }


    @Override
    public String toString() {
        return "Environment{" +
                "uuid=" + uuid +
                ", nodes=" + nodes +
                ", name='" + name + '\'' +
                '}';
    }
}
