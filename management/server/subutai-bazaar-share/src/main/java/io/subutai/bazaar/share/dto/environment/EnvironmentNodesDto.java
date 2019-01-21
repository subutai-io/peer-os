package io.subutai.bazaar.share.dto.environment;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class EnvironmentNodesDto
{
    private String peerId;

    private String environmentId;

    private List<EnvironmentNodeDto> nodes = new ArrayList<>();


    public EnvironmentNodesDto()
    {
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    public List<EnvironmentNodeDto> getNodes()
    {
        return nodes;
    }


    public void setNodes( final List<EnvironmentNodeDto> nodes )
    {
        this.nodes = nodes;
    }


    public void addNode( final EnvironmentNodeDto nodeDto )
    {
        this.nodes.add( nodeDto );
    }


    public void removeNode( final EnvironmentNodeDto nodeDto )
    {
        this.nodes.remove( nodeDto );
    }
}
