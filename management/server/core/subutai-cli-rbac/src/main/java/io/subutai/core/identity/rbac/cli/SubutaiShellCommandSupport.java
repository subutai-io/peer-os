package io.subutai.core.identity.rbac.cli;


import java.util.Set;

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.CliCommand;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.Role;
import io.subutai.core.identity.api.User;
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

            this.session = session;
            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
            if ( identityManager != null )
            {
                User user = identityManager.getUser();
                CliCommand temp = identityManager.createCliCommand( command.scope(), command.name() );
                if ( user != null )
                {
                    Set<Role> roles = user.getRoles();
                    for ( final Role role : roles )
                    {
                        if ( role.getCliCommands().contains( temp ) )
                        {
                            return doExecute();
                        }
                    }
                }
            }
            LOG.warn( "Access denied." );
            return null;
        }
        finally
        {
            ungetServices();
        }
    }
}
