package org.safehaus.subutai.api.manager.helper;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 6/24/14.
 */
public class Environment extends Blueprint {

    private String owner;
    private UUID uuid;
    private Set<Agent> agents;


    public Environment() {
        this.uuid = UUID.randomUUID();
        this.agents = new HashSet<>();
    }


    public UUID getUuid() {
        return uuid;
    }


    public void setUuid( final UUID uuid ) {
        this.uuid = uuid;
    }


    public Set<Agent> getAgents() {
        return agents;
    }


    public void setAgents( final Set<Agent> agents ) {
        this.agents = agents;
    }


    @Override
    public String toString() {
        return "Environment{" +
                "owner='" + owner + '\'' +
                ", uuid=" + uuid +
                '}';
    }
}
