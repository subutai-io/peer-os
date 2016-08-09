package io.subutai.common.resource;


import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Historical metrics series batch
 */
public class SeriesBatch
{
    @JsonProperty("Series")
    Series[] series;

    @JsonProperty("Messages")
    String messages;


    public Series[] getSeries()
    {
        return series;
    }


    public String getMessages()
    {
        return messages;
    }
}
