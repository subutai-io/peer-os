package io.subutai.core.test.cli;


import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Command;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.command.Response;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "test", name = "do", description = "test command" )
public class TestCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( TestCommand.class.getName() );

    //    @Argument( index = 0, name = "term", required = true, multiValued = false,
    //            description = "term to search" )
    //    String term;


    @Override
    protected Object doExecute()
    {

        try
        {
            LocalPeer localPeer = ServiceLocator.getServiceNoCache( LocalPeer.class );

            localPeer.getManagementHost().execute( new RequestBuilder( "pwd" ), new CommandCallback()
            {
                @Override
                public void onResponse( final Response response, final CommandResult commandResult )
                {
                    IdentityManager identityManager = null;
                    try
                    {
                        identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
                    }
                    catch ( NamingException e )
                    {
                    }
                    LOG.error( identityManager.getActiveUser().toString() );
                }
            } );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in test", e );
        }

        return null;
    }
}
