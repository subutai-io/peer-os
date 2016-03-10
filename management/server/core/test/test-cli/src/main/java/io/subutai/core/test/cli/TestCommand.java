package io.subutai.core.test.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.test.appender.SolAppender;


@Command( scope = "test", name = "do", description = "test command" )
public class TestCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( TestCommand.class.getName() );

    @Argument( index = 0, name = "log error", required = false, multiValued = false,
            description = "log error" )
    boolean logError = false;


    @Override
    protected Object doExecute()
    {

        try
        {
            if ( logError )
            {
                LOG.error( "REQUESTED ERROR", new RuntimeException( "blablabla" ) );
            }
            else
            {
                for ( SolAppender.SubutaiLogEvent loggingEvent : SolAppender.getLoggingEvents() )
                {
                    System.out.println( loggingEvent );
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in test", e );
        }

        return null;
    }
}
