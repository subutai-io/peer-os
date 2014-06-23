package org.safehaus.subutai.api.manager;


import java.util.Set;


/**
 * Created by bahadyr on 6/24/14.
 */
public class EnvironmentNodeGroup {



    Set<EnvironmentGroupInstance> environmentGroupInstanceSet;


    public Set<EnvironmentGroupInstance> getEnvironmentGroupInstanceSet() {
        return environmentGroupInstanceSet;
    }


    public void setEnvironmentGroupInstanceSet( final Set<EnvironmentGroupInstance> environmentGroupInstanceSet ) {
        this.environmentGroupInstanceSet = environmentGroupInstanceSet;
    }
}
