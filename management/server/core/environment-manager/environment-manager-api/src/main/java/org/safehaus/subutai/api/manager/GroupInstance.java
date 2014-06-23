package org.safehaus.subutai.api.manager;


import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 6/24/14.
 */
public class GroupInstance {

    private String name;
    private CreationStrateyENUM creationStrateyENUM;
    private Agent agent;


    public String getName() {
        return name;
    }


    public void setName( final String name ) {
        this.name = name;
    }


    public Agent getAgent() {
        return agent;
    }


    public void setAgent( final Agent agent ) {
        this.agent = agent;
    }


    public CreationStrateyENUM getCreationStrateyENUM() {
        return creationStrateyENUM;
    }


    public void setCreationStrateyENUM( final CreationStrateyENUM creationStrateyENUM ) {
        this.creationStrateyENUM = creationStrateyENUM;
    }
}
