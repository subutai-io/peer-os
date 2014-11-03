package org.safehaus.subutai.common.exception;


/**
 * Created by bahadyr on 9/19/14.
 */
public class SubutaiException extends Exception
{
    public SubutaiException( final String message )
    {
        super( message );
    }


    public SubutaiException( final Throwable cause )
    {
        super( cause );
    }
}
