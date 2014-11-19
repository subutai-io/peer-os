package org.safehaus.subutai.wol.api;

/**
 * Created by emin on 11/17/14.
 */
public class WolManagerException extends Exception  {
    public WolManagerException( final String message )
    {
        super( message );
    }

    public WolManagerException( final Throwable cause )
    {
        super( cause );
    }
}
