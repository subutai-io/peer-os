package io.subutai.hub.share.resource;


import com.fasterxml.jackson.annotation.JsonProperty;


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
