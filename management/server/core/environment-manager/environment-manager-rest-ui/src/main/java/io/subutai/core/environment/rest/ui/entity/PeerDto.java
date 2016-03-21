package io.subutai.core.environment.rest.ui.entity;

import java.util.ArrayList;
import java.util.List;

public class PeerDto
{
    private int id;
    private String name;
    private List<ResourceHostDto> resourceHosts;

    public PeerDto(int id, String name) {
        this.id = id;
        this.name = name;
        resourceHosts = new ArrayList<>();
    }

    public void addResourceHostDto( ResourceHostDto rh )
    {
        resourceHosts.add( rh );
    }
}
