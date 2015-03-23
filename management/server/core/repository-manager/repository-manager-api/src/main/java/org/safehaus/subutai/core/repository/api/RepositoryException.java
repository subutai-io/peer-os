package org.safehaus.subutai.core.repository.api;


/**
 * Exception that might be thrown by Repository Manager
 */
public class RepositoryException extends Exception
{
    public RepositoryException( final String message )
    {
        super( message );
    }


    public RepositoryException( final Throwable cause )
    {
        super( cause );
    }


    public RepositoryException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
