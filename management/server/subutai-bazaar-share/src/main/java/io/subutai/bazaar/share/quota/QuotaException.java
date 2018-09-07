package io.subutai.bazaar.share.quota;


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


}
