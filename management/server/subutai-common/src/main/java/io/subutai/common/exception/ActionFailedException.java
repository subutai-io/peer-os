package io.subutai.common.exception;


public class ActionFailedException extends RuntimeException
{
    public ActionFailedException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public ActionFailedException( final String message )
    {
        super( message );
    }
}
