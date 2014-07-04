package org.safehaus.subutai.api.manager.helper;


import java.util.Set;


/**
 * Created by bahadyr on 6/23/14.
 */
public class EnvironmentBlueprint extends Blueprint {


    Set<NodeGroup> nodeGroups;


    public Set<NodeGroup> getNodeGroups() {
        return nodeGroups;
    }


    public void setNodeGroups( final Set<NodeGroup> nodeGroups ) {
        this.nodeGroups = nodeGroups;
    }
}
