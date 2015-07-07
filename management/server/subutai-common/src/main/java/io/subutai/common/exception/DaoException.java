package io.subutai.common.exception;


/**
 * Exception thrown by DispatcherDAO
 */
public class DaoException extends Exception
{
    public DaoException( final Throwable cause )
    {
        super( cause );
    }


    public DaoException( final String message )
    {
        super( message );
    }
}
