package org.safehaus.subutai.api.manager;


import java.util.Set;


/**
 * Created by bahadyr on 6/24/14.
 */
public class Environment {

    private String name;
    private Set<NodeGroup> nodeGroups;


    public String getName() {
        return name;
    }


    public void setName( final String name ) {
        this.name = name;
    }


    public Set<NodeGroup> getNodeGroups() {
        return nodeGroups;
    }


    public void setNodeGroups( final Set<NodeGroup> nodeGroups ) {
        this.nodeGroups = nodeGroups;
    }
}
