package io.subutai.common.environment;


import java.util.List;


public class PrepareTemplatesResponse
{
    private boolean result;
    private String description;


    public PrepareTemplatesResponse( final boolean result, final String description )
    {
        this.result = result;
        this.description = description;
    }


    public boolean getResult()
    {
        return result;
    }


    public String getDescription()
    {
        return description;
    }
}
