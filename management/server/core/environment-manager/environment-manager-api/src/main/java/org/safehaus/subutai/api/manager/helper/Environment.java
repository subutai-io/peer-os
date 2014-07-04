package org.safehaus.subutai.api.manager.helper;


import java.util.Date;
import java.util.Set;


/**
 * Created by bahadyr on 6/24/14.
 */
public class Environment extends Blueprint {

    private Set<EnvironmentNodeGroup> environmentNodeGroups;
    private String owner;
    private Date creationDate;


    public Set<EnvironmentNodeGroup> getEnvironmentNodeGroups() {
        return environmentNodeGroups;
    }


    public void setEnvironmentNodeGroups( final Set<EnvironmentNodeGroup> environmentNodeGroups ) {
        this.environmentNodeGroups = environmentNodeGroups;
    }


    @Override
    public String toString() {
        return "Environment{" +
                "environmentNodeGroups=" + environmentNodeGroups +
                ", owner='" + owner + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}
