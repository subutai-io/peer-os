package org.safehaus.subutai.api.manager;


import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 6/24/14.
 */
public class GroupInstance {

    private String name;
    private Agent agent;


    public String getName() {
        return name;
    }


    public void setName( final String name ) {
        this.name = name;
    }
}
