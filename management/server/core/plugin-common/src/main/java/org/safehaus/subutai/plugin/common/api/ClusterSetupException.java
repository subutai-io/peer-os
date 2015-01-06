package org.safehaus.subutai.plugin.common.api;


import org.safehaus.subutai.common.exception.SubutaiException;


/**
 * Created by dilshat on 7/21/14.
 */
public class ClusterSetupException extends SubutaiException
{

    public ClusterSetupException( final String message )
    {
        super( message );
    }


    public ClusterSetupException( final Throwable cause )
    {
        super( cause );
    }
}
