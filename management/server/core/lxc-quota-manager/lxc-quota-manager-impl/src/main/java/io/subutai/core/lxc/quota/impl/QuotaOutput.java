package io.subutai.core.lxc.quota.impl;


import org.codehaus.jackson.annotate.JsonProperty;


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
