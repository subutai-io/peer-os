package org.safehaus.subutai.core.environment.api.topology;


import java.util.UUID;


public abstract class TopologyData
{

    UUID blueprintId;


    public UUID getBlueprintId()
    {
        return blueprintId;
    }


    public void setBlueprintId( UUID blueprintId )
    {
        this.blueprintId = blueprintId;
    }


}

