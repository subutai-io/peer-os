package io.subutai.core.localpeer.impl.binding;


import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Commands batch output
 */
public class BatchOutput<T>
{
    @JsonProperty( "output" )
    private T output;
    @JsonProperty( "exitcode" )
    private Integer exitcode;


    public BatchOutput( @JsonProperty( "output" ) final T output, @JsonProperty( "exitcode" ) Integer exitcode )
    {
        this.output = output;
        this.exitcode = exitcode;
    }


    public T getOutput()
    {
        return output;
    }


    public Integer getExitcode()
    {
        return exitcode;
    }
}
