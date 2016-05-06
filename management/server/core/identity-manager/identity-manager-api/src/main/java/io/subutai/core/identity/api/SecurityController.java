package io.subutai.core.identity.api;


/**
 * Logs and Checks security events
 */
public interface SecurityController
{
    //****************************
    void logEvent( String userName, String action );


    int checkTrustLevel( String fingeprint );


    String getGlobalkeyServer();


    void setGlobalkeyServer( String globalkeyServer );


    String getLocalkeyServer();


    void setLocalkeyServer( String localkeyServer );
}
