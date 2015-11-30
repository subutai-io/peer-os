package io.subutai.common.resource;


import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Historical metrics
 */
public class HistoricalMetrics
{
    @JsonProperty( "Metrics" )
    List<SeriesBatch> metrics = new ArrayList<>();


    public HistoricalMetrics()
    {
    }


    public HistoricalMetrics( @JsonProperty( "Metrics" ) final List<SeriesBatch> metrics )
    {
        this.metrics = metrics;
    }


    public List<SeriesBatch> getMetrics()
    {
        return metrics;
    }
}
