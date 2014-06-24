package org.safehaus.subutai.api.manager.helper;


import java.util.Set;


/**
 * Created by bahadyr on 6/24/14.
 */
public class Environment {

    private String name;
    private Set<EnvironmentNodeGroup> environmentNodeGroups;


    public String getName() {
        return name;
    }


    public void setName( final String name ) {
        this.name = name;
    }


    public Set<EnvironmentNodeGroup> getEnvironmentNodeGroups() {
        return environmentNodeGroups;
    }


    public void setEnvironmentNodeGroups( final Set<EnvironmentNodeGroup> environmentNodeGroups ) {
        this.environmentNodeGroups = environmentNodeGroups;
    }
}
