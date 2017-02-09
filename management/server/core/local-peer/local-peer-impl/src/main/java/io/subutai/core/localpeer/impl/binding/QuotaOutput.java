package io.subutai.core.localpeer.impl.binding;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;


public class QuotaOutput
{
    @JsonProperty( "quota" )
    private String quota;
    @JsonProperty( "threshold" )
    private Integer threshold;


    @JsonCreator
    public static QuotaOutput create( String jsonString )
    {
        QuotaOutput output = null;

        try
        {
            output = new ObjectMapper().readValue( jsonString, QuotaOutput.class );
        }
        catch ( Exception e )
        {
            // handle
        }

        return output;
    }


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


