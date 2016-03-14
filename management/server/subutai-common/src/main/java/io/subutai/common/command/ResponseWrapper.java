package io.subutai.common.command;


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
