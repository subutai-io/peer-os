package io.subutai.core.executor.impl;


import io.subutai.common.command.ResponseImpl;


/**
 * Wrapper to serialize a response
 */
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
