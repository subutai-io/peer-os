package org.safehaus.subutai.plugin.mongodb.api;


import org.safehaus.subutai.common.exception.SubutaiException;


public class MongoException extends SubutaiException
{
    public MongoException( final String message )
    {
        super( message );
    }
}
