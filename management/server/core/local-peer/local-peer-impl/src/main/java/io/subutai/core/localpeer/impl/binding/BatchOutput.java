package io.subutai.core.localpeer.impl.binding;


import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Commands batch output
 */
public class BatchOutput
{
    @JsonProperty( "output" )
    private QuotaOutput output;
    @JsonProperty( "exitcode" )
    private Integer exitcode;


    public BatchOutput( @JsonProperty( "output" ) final QuotaOutput output,
                        @JsonProperty( "exitcode" ) Integer exitcode )
    {
        this.output = output;
        this.exitcode = exitcode;
    }


    public QuotaOutput getOutput()
    {
        return output;
    }


    public Integer getExitcode()
    {
        return exitcode;
    }
}
