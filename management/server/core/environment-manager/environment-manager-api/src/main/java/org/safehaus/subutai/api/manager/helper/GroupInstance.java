package org.safehaus.subutai.api.manager.helper;


/**
 * Created by bahadyr on 6/24/14.
 */
public class GroupInstance {

    private String name;
    private PlacementStrategyENUM creationStrateyENUM;


    public String getName() {
        return name;
    }


    public void setName( final String name ) {
        this.name = name;
    }


    public PlacementStrategyENUM getCreationStrateyENUM() {
        return creationStrateyENUM;
    }


    public void setCreationStrateyENUM( final PlacementStrategyENUM creationStrateyENUM ) {
        this.creationStrateyENUM = creationStrateyENUM;
    }
}
