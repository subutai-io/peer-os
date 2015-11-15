package io.subutai.core.identity.rbac.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Filters out karaf user shell command accessibility according to his/her role permission Check is made upon existence
 * of a command pattern in identity registry
 */
public abstract class SubutaiShellCommandSupport extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( SubutaiShellCommandSupport.class );


    @Override
    public Object execute( final CommandSession session ) throws Exception
    {
        try
        {
            Command command = this.getClass().getAnnotation( Command.class );
            LOG.debug( String.format( "Executing command: %s:%s", command.scope(), command.name() ) );

            return doExecute();
        }
        finally
        {
            ungetServices();
        }
    }
}
