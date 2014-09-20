package org.safehaus.subutai.plugin.storm.impl;


public enum StormService
{

    NIMBUS( "storm-nimbus" ),
    UI( "storm-ui" ),
    SUPERVISOR( "storm-supervisor" );

    private final String service;


    private StormService( String service )
    {
        this.service = service;
    }


    public String getService()
    {
        return service;
    }


    @Override
    public String toString()
    {
        String s = super.toString();
        return s.substring( 0, 1 ).toUpperCase() + s.substring( 1 ).toLowerCase();
    }

}
