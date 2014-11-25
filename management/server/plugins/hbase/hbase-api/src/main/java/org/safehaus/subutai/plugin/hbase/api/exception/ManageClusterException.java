package org.safehaus.subutai.plugin.hbase.api.exception;


import org.safehaus.subutai.common.command.CommandException;


/**
 * Created by bahadyr on 11/18/14.
 */
public class ManageClusterException extends Exception
{

    public ManageClusterException( final String message )
    {
        super( message );
    }
}
