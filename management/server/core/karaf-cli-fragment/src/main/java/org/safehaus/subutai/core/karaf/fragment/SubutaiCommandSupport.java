package org.safehaus.subutai.core.karaf.fragment;


import java.util.Set;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.identity.api.CliCommand;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.Role;
import org.safehaus.subutai.core.identity.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by talas on 3/12/15.
 */
public abstract class SubutaiCommandSupport extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( SubutaiCommandSupport.class );


    @Override
    public Object execute( final CommandSession session ) throws Exception
    {
        try
        {
            Command command = this.getClass().getAnnotation( Command.class );
            LOG.warn( String.format( "Executing command: %s:%s", command.scope(), command.name() ) );

            this.session = session;
            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
            if ( identityManager != null )
            {
                User user = identityManager.getUser();
                CliCommand temp = identityManager.createMockCliCommand( command.scope(), command.name() );
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
            return null;
        }
        finally
        {
            ungetServices();
        }
    }
}
