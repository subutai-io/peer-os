package org.safehaus.subutai.api.manager.helper;


import java.util.Set;


/**
 * Created by bahadyr on 6/23/14.
 */
public class EnvironmentBlueprint {


    private String name;
    Set<NodeGroup> nodeGroups;


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
