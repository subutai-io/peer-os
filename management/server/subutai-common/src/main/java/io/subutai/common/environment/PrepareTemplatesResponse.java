package io.subutai.common.environment;


import java.util.List;


public class PrepareTemplatesResponse
{
    private boolean result;
    private String description;
    private List<String> exceptions;


    public PrepareTemplatesResponse( final boolean result, final String description, final List<String> exceptions )
    {
        this.result = result;
        this.description = description;
        this.exceptions = exceptions;
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
