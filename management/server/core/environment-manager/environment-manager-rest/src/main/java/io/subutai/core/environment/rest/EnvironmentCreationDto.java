package io.subutai.core.environment.rest;


import java.util.List;


public class EnvironmentCreationDto
{
    private String environmentName;

    private List<NodeDto> nodes;


    public String getEnvironmentName()
    {
        return environmentName;
    }


    List<NodeDto> getNodes()
    {
        return nodes;
    }
}
