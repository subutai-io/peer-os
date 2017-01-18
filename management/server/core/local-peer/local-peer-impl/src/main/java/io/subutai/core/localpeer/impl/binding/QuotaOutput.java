package io.subutai.core.localpeer.impl.binding;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;


public class QuotaOutput
{
    @JsonProperty( "output" )
    private Output output;
    @JsonProperty( "exitcode" )
    private Integer exitcode;


    public QuotaOutput( @JsonProperty( "output" ) final Output output, @JsonProperty( "exitcode" ) Integer exitcode )
    {
        this.output = output;
        this.exitcode = exitcode;
    }


    public Output getOutput()
    {
        return output;
    }


    public Integer getExitcode()
    {
        return exitcode;
    }


    public static class Output
    {
        @JsonProperty( "quota" )
        private String quota;
        @JsonProperty( "threshold" )
        private Integer threshold;


        @JsonCreator
        public static Output create( String jsonString )
        {
            Output output = null;

            try
            {
                output = new ObjectMapper().readValue( jsonString, Output.class );
            }
            catch ( Exception e )
            {
                // handle
            }

            return output;
        }


        public Output( @JsonProperty( "quota" ) final String quota,
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
}


