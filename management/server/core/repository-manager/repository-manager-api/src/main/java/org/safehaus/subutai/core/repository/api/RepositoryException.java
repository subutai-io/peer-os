package org.safehaus.subutai.core.repository.api;


import org.safehaus.subutai.common.exception.SubutaiException;


/**
 * Exception that might be thrown by Repository Manager
 */
public class RepositoryException extends SubutaiException
{
    public RepositoryException( final String message )
    {
        super( message );
    }


    public RepositoryException( final Throwable cause )
    {
        super( cause );
    }
}
