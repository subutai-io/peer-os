package io.subutai.core.localpeer.impl.binding;


import com.fasterxml.jackson.annotation.JsonProperty;


public class QuotaOutput
{
    @JsonProperty( "quota" )
    private String quota;
    @JsonProperty( "threshold" )
    private Integer threshold;


    public QuotaOutput( @JsonProperty( "quota" ) final String quota,
                        @JsonProperty( "threshold" ) final Integer threshold )
    {
        this.quota = quota;
        this.threshold = threshold;
    }


    public String getQuota()
    {
        return quota;
    }


    public Integer getThreshold()
    {
        return threshold;
    }
}
