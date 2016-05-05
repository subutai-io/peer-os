package io.subutai.core.identity.api;


/**
 * Logs and Checks security events
 */
public interface SecurityController
{
    //****************************
    void logEvent( String userName, String action );
}
