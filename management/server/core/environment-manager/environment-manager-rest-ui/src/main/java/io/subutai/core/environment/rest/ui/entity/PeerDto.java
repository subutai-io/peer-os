package io.subutai.core.environment.rest.ui.entity;


import java.util.ArrayList;
import java.util.List;


public class PeerDto
{
    private String id;
    private String name;
    //contains metrics of CONNECTED resource hosts
    private List<ResourceHostDto> resourceHosts;
    private boolean isOnline;
    private boolean isLocal;
    //contains count of ALL resource hosts
    private int rhCount;


    public PeerDto( String id, String name, boolean isOnline, boolean isLocal )
    {
        this.id = id;
        this.name = name;
        this.isOnline = isOnline;
        this.isLocal = isLocal;
        resourceHosts = new ArrayList<>();
    }


    public void addResourceHostDto( ResourceHostDto rh )
    {
        resourceHosts.add( rh );
    }


    public List<ResourceHostDto> getResourceHosts()
    {
        return resourceHosts;
    }


    public int getRhCount()
    {
        return rhCount;
    }


    public void setRhCount( final int rhCount )
    {
        this.rhCount = rhCount;
    }


    public String getName()
    {
        return name;
    }
}
