package io.subutai.common.resource;


import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Historical metrics series batch
 */
public class SeriesBatch
{
    @JsonProperty("Series")
    Series[] series;
    @JsonProperty("Err")
    String error;


    public Series[] getSeries()
    {
        return series;
    }


    public String getError()
    {
        return error;
    }
}
