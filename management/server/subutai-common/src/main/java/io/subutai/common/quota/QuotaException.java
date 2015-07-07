package io.subutai.common.quota;


public class QuotaException extends Exception
{
    public QuotaException()
    {
        super();
    }


    public QuotaException( String message )
    {
        super( "QuotaManager" + message );
    }


    public QuotaException( Throwable cause )
    {
        super( cause );
    }


    public QuotaException( String message, Throwable cause )
    {
        super( message, cause );
    }


    public QuotaException( String message, QuotaType parameter )
    {
        super( parameter.getKey() + message );
    }


    public QuotaException( String message, Throwable cause, QuotaType parameter )
    {
        super( parameter.getKey() + message, cause );
    }
}
