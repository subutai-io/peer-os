package io.subutai.core.environment.rest;


import java.util.List;

import com.google.gson.annotations.SerializedName;


public class EnvironmentCreationDto
{
    @SerializedName( "name" )
    private String environmentName;

    private List<NodeDto> nodes;

    private String sshKey;


    public String getEnvironmentName()
    {
        return environmentName;
    }


    List<NodeDto> getNodes()
    {
        return nodes;
    }


    public String getSshKey()
    {
        return sshKey;
    }
}
