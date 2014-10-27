package org.safehaus.subutai.core.configuration.cli;


import org.safehaus.subutai.core.configuration.api.TextInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "config", name = "echo", description = "Executes cat command on given host" )
public class EchoCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( EchoCommand.class );
    @Argument( index = 0, name = "hostname", required = true, multiValued = false, description = "Agent hostname" )
    String hostname;
    @Argument( index = 1, name = "pathToFile", required = true, multiValued = false, description = "Path to file" )
    String pathToFile;
    @Argument( index = 2, name = "content", required = true, multiValued = false, description = "File content" )
    String content;
    private TextInjector textInjector;


    public TextInjector getTextInjector()
    {
        return textInjector;
    }


    public void setTextInjector( final TextInjector textInjector )
    {
        this.textInjector = textInjector;
    }


    protected Object doExecute()
    {
        Boolean result = textInjector.echoTextIntoAgent( hostname, pathToFile, content );
        LOG.info( "EchoCommand@doExecute: " + result );
        return null;
    }
}
