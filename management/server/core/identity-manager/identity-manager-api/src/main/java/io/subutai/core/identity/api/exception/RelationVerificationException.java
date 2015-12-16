package io.subutai.core.identity.api.exception;


/**
 * Created by talas on 12/13/15.
 */
public class RelationVerificationException extends Exception
{
    public RelationVerificationException()
    {
        super();
    }


    public RelationVerificationException( final String message )
    {
        super( message );
    }


    public RelationVerificationException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public RelationVerificationException( final Throwable cause )
    {
        super( cause );
    }


    public RelationVerificationException( final String message, final Throwable cause, final boolean enableSuppression,
                                          final boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
