package io.subutai.common.exception;


/**
 * Exception thrown by DAO classes
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
