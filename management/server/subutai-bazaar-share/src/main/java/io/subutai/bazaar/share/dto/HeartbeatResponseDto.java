package io.subutai.bazaar.share.dto;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
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
