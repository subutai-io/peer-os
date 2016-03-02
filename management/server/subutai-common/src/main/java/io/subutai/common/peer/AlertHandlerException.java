package io.subutai.common.peer;


/**
 * Exception on alert handling
 */
public class AlertHandlerException extends Exception
{
    public AlertHandlerException( String msg )
    {
        super( msg );
    }


    public AlertHandlerException( String msg, Throwable e )
    {
        super( msg, e );
    }
}
