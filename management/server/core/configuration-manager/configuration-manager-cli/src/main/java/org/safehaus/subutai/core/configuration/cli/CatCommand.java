package org.safehaus.subutai.core.configuration.cli;


import org.safehaus.subutai.core.configuration.api.TextInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "config", name = "cat", description = "Executes cat command on given host" )
public class CatCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( CatCommand.class );
    @Argument( index = 0, name = "hostname", required = true, multiValued = false, description = "Agent hostname" )
    String hostname;
    @Argument( index = 1, name = "pathToFile", required = true, multiValued = false, description = "Path to file" )
    String pathToFile;
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
        String fileContent = textInjector.catFile( hostname, pathToFile );
        LOG.info( fileContent );
        return null;
    }
}
