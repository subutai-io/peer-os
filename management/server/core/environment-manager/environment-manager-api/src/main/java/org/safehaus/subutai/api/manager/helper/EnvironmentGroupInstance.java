package org.safehaus.subutai.api.manager.helper;


import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 6/24/14.
 */
public class EnvironmentGroupInstance {


    private String name;
    private Agent agent;
    private PlacementStrategyENUM placementStrategyENUM;


    public PlacementStrategyENUM getPlacementStrategyENUM() {
        return placementStrategyENUM;
    }


    public void setPlacementStrategyENUM( final PlacementStrategyENUM placementStrategyENUM ) {
        this.placementStrategyENUM = placementStrategyENUM;
    }


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
}
