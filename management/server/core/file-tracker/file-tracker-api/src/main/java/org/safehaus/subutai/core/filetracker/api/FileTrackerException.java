package org.safehaus.subutai.core.filetracker.api;


import org.safehaus.subutai.common.exception.SubutaiException;


public class FileTrackerException extends SubutaiException
{
    public FileTrackerException( final String message )
    {
        super( message );
    }


    public FileTrackerException( final Throwable cause )
    {
        super( cause );
    }
}
