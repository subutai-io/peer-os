package io.subutai.bazaar.share.dto;


import java.util.HashSet;
import java.util.Set;


public class HeartbeatResponseDto
{
    private final HashSet<String> stateLinks = new HashSet<>();


    public HeartbeatResponseDto()
    {
    }


    public Set<String> getStateLinks()
    {
        return stateLinks;
    }
}
