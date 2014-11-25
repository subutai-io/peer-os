package org.safehaus.subutai.core.executor.impl;


public class ResponseWrapper
{
    private final ResponseImpl response;


    public ResponseWrapper( final ResponseImpl response )
    {
        this.response = response;
    }


    public ResponseImpl getResponse()
    {
        return response;
    }
}
