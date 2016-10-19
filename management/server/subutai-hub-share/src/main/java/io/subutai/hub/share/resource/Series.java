package io.subutai.hub.share.resource;


import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;


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

    @JsonProperty( "columns" )
    List<String> columns;

    @JsonProperty( "values" )
    private List<List<Double>> values;


    public Series()
    {
    }


    public Series( @JsonProperty( "name" ) final String name, @JsonProperty( "tags" ) Map<String, String> tags,
                   @JsonProperty( "columns" ) List<String> columns,
                   @JsonProperty( "values" ) final List<List<Double>> values )
    {
        this.name = name;
        this.tags = tags;
        this.values = values;
        this.columns = columns;
    }


    public List<String> getColumns()
    {
        return columns;
    }


    public String getName()
    {
        return name;
    }


    public Map<String, String> getTags()
    {
        return tags;
    }


    public List<List<Double>> getValues()
    {
        return values;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "name", name ).add( "tags", tags ).add( "columns", columns )
                          .add( "values", values ).toString();
    }
}

