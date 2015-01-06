package org.safehaus.subutai.plugin.common.api;


import org.safehaus.subutai.common.exception.SubutaiException;


/**
 * Created by dilshat on 7/23/14.
 */
public class ClusterConfigurationException extends SubutaiException
{

    public ClusterConfigurationException( final String message )
    {
        super( message );
    }


    public ClusterConfigurationException( final Throwable cause )
    {
        super( cause );
    }
}
