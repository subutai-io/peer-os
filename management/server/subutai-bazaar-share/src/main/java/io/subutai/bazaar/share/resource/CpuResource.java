package io.subutai.bazaar.share.resource;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * CPU resource class
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class CpuResource extends Resource<NumericValueResource>
{
    @JsonProperty( "model" )
    private String model;               // model
    @JsonProperty( "availableProcessors" )
    private int availableProcessors;    // count of available cores
    @JsonProperty( "l1CacheSize" )
    private int l1CacheSize;            // L1 cache size in MB
    @JsonProperty( "l2CacheSize" )
    private int l2CacheSize;            // L2 cache size in MB
    @JsonProperty( "l3CacheSize" )
    private int l3CacheSize;            // L3 cache size in MB
    @JsonProperty( "frequency" )
    private double frequency;           // frequency of CPU
    @JsonProperty( "avgLoad" )
    private double avgLoad;             // average load of CPU


    public CpuResource( @JsonProperty( "value" ) final BigDecimal availableCpu,
                        @JsonProperty( "cost" ) final Double cost, @JsonProperty( "model" ) final String model,
                        @JsonProperty( "availableProcessors" ) final int availableProcessors,
                        @JsonProperty( "l1CacheSize" ) final int l1CacheSize,
                        @JsonProperty( "l2CacheSize" ) final int l2CacheSize,
                        @JsonProperty( "l3CacheSize" ) final int l3CacheSize,
                        @JsonProperty( "frequency" ) final double frequency,
                        @JsonProperty( "avgLoad" ) final double avgLoad )
    {
        super( new NumericValueResource( availableCpu ), ResourceType.CPU, cost );
        this.model = model;
        this.availableProcessors = availableProcessors;
        this.l1CacheSize = l1CacheSize;
        this.l2CacheSize = l2CacheSize;
        this.l3CacheSize = l3CacheSize;
        this.frequency = frequency;
        this.avgLoad = avgLoad;
    }


    public String getModel()
    {
        return model;
    }


    public int getAvailableProcessors()
    {
        return availableProcessors;
    }


    public int getL1CacheSize()
    {
        return l1CacheSize;
    }


    public int getL2CacheSize()
    {
        return l2CacheSize;
    }


    public int getL3CacheSize()
    {
        return l3CacheSize;
    }


    public double getFrequency()
    {
        return frequency;
    }


    public double getAvgLoad()
    {
        return avgLoad;
    }


    @Override
    public String getWriteValue()
    {
        return String.format( "%5.2f", resourceValue.getValue() );
    }


    @Override
    public String getPrintValue()
    {
        return String.format( "%5.2f%%", resourceValue.getValue() );
    }
}
