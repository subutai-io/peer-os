package io.subutai.core.test.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Created by talas on 12/6/15.
 */
@Command( scope = "test", name = "verify", description = "test command" )
public class TestRoleValidation extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( TestCommand.class.getName() );


    @Override
    public Object execute( final CommandSession session ) throws Exception
    {
        try
        {
            System.out.println( "Hello there!" );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in test", e );
        }

        return null;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        return null;
    }
}
