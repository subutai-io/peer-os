package org.safehaus.subutai.core.lxc.quota.api;


import org.safehaus.subutai.common.quota.QuotaType;


/**
 * Created by talas on 10/7/14.
 */
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
