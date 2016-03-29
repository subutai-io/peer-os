package io.subutai.core.environment.rest.ui.entity;

import java.util.ArrayList;
import java.util.List;

public class PeerDto
{
    private String id;
    private String name;
    private List<ResourceHostDto> resourceHosts;
    private boolean isOnline;
    private boolean isLocal;

    public PeerDto(String id, String name, boolean isOnline, boolean isLocal) {
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
}
