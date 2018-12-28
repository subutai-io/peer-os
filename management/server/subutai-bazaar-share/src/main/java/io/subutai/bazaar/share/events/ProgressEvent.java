package io.subutai.bazaar.share.events;


public class ProgressEvent
{
    private String process;

    private String step;

    private String message;

    private double value;


    public String getProcess()
    {
        return process;
    }


    public void setProcess( final String process )
    {
        this.process = process;
    }


    public String getStep()
    {
        return step;
    }


    public void setStep( final String step )
    {
        this.step = step;
    }


    public String getMessage()
    {
        return message;
    }


    public void setMessage( final String message )
    {
        this.message = message;
    }


    public double getValue()
    {
        return value;
    }


    public void setValue( final double value )
    {
        this.value = value;
    }
}
