package io.subutai.common.protocol;


import com.fasterxml.jackson.annotation.JsonProperty;


public class Template
{
    @JsonProperty( "id" )
    private String id;
    @JsonProperty( "name" )
    private String name;


    public String getId()
    {
        return id;
    }


    public String getName()
    {
        return name;
    }
}
