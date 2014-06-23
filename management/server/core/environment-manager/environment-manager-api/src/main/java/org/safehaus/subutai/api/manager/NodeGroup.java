package org.safehaus.subutai.api.manager;


import java.util.Set;


/**
 * Created by bahadyr on 6/24/14.
 */
public class NodeGroup {

    private String name;
    Set<GroupInstance> groupInstanceSet;


    public Set<GroupInstance> getGroupInstances() {
        return groupInstanceSet;
    }


    public String getName() {
        return name;
    }


    public void setName( final String name ) {
        this.name = name;
    }
}
