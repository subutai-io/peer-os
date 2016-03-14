package io.subutai.core.test.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "test", name = "do", description = "test command" )
public class TestCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( TestCommand.class.getName() );


    @Argument( index = 0, name = "log error", required = false, multiValued = false,
            description = "log error" )
    boolean logError = false;
    @Argument( index = 1, name = "throw error", required = false, multiValued = false,
            description = "throw error" )
    boolean throwError = false;


    @Override
    protected Object doExecute()
    {

        if ( logError )
        {
            LOG.error( "REQUESTED ERROR", new RuntimeException( "blablabla" ) );
        }

        if ( throwError )
        {
            throw new RuntimeException( "OOOOOOOPS" );
        }

        return null;
    }
}
