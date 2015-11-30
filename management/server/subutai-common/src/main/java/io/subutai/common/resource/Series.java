package io.subutai.common.resource;


import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Historical metric series
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class Series
{
    @JsonProperty( "name" )
    String name;

    @JsonProperty( "tags" )
    Map<String, String> tags;

    @JsonProperty( "values" )
    private List<List<String>> values;


    public Series( @JsonProperty( "name" ) final String name, @JsonProperty( "rags" ) Map<String, String> tags,
                   @JsonProperty( "values" ) final List<List<String>> values )
    {
        this.name = name;
        this.tags = tags;
        this.values = values;
    }


    public String getName()
    {
        return name;
    }


    public Map<String, String> getTags()
    {
        return tags;
    }


    public List<List<String>> getValues()
    {
        return values;
    }
}

