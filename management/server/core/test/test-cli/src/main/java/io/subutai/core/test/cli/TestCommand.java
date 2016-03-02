package io.subutai.core.test.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.test.appender.SolAppender;


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
            for ( String loggingEvent : SolAppender.getLoggingEvents() )
            {
                System.out.println( loggingEvent );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in test", e );
        }

        return null;
    }
}
