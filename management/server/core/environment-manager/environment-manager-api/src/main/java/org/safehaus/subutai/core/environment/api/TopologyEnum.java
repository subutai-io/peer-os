package org.safehaus.subutai.core.environment.api;


public enum TopologyEnum
{

    NODE_2_PEER( "Node to Peer" ),
    NODE_GROUP_2_PEER( "Node Group to Peer" ),
    BLUEPRINT_2_PEER( "Blueprint to Peer" ),
    BLUEPRINT_2_PEER_GROUP( "Blueprint 2 Peer Group" ),
    NODE_GROUP_2_PEER_GROUP( "Node Group 2 Peer Group" );

    String description;


    TopologyEnum( final String description )
    {
        this.description = description;
    }


    public String getDescription()
    {
        return description;
    }
}
