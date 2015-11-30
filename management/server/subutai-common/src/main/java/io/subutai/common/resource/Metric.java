package io.subutai.common.resource;


import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Historical metrics
 */
public class Metric
{
    @JsonProperty("Metrics")
    SeriesBatch[] metrics;


    public Metric(@JsonProperty("Metrics") final SeriesBatch[] metrics )
    {
        this.metrics = metrics;
    }


    public SeriesBatch[] getMetrics()
    {
        return metrics;
    }
}
