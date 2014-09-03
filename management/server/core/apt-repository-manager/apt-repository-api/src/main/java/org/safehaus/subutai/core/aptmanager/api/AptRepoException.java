package org.safehaus.subutai.core.aptmanager.api;


/**
 * Represents exception thrown by AptRepositoryManager
 */
public class AptRepoException extends Exception {

    public AptRepoException( final String message ) {
        super( message );
    }
}
