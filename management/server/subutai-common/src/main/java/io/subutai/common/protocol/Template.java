package io.subutai.common.protocol;


import org.codehaus.jackson.annotate.JsonProperty;


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
