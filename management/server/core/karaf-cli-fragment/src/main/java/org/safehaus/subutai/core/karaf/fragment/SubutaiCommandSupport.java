package org.safehaus.subutai.core.karaf.fragment;


import java.util.Set;

import org.safehaus.subutai.common.util.ServiceLocator;
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
        LOG.warn( "Executing command", session.toString() );
        try
        {
            Command command = this.getClass().getAnnotation( Command.class );
            command.scope();
            command.name();


            this.session = session;
            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
            if ( identityManager != null )
            {
                User user = identityManager.getUser();
                if ( user != null )
                {
                    Set<Role> roles = user.getRoles();
                    for ( final Role role : roles )
                    {
                    }
                }
            }
            if ( identityManager.getUser().isAdmin() )
            {
                return doExecute();
            }
            else
            {
                return null;
            }
        }
        finally
        {
            ungetServices();
        }
    }
}
