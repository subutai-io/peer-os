package org.safehaus.subutai.core.configuration.cli;


import org.safehaus.subutai.core.configuration.api.ConfiguraitonTypeEnum;
import org.safehaus.subutai.core.configuration.api.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "config", name = "echo-text-to-file",
        description = "Echo text into a file" )
public class EchoTextToFileCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( EchoTextToFileCommand.class );
    @Argument( index = 0, name = "hostname", required = true, multiValued = false, description = "Path to file" )
    String hostname;
    @Argument( index = 1, name = "json", required = true, multiValued = false, description = "Path to file" )
    String json;
    @Argument( index = 2, name = "pathToFile", required = true, multiValued = false, description = "Path to file" )
    String pathToFile;
    @Argument( index = 3, name = "type", required = true, multiValued = false, description = "Configuration type" )
    String type;

    private ConfigurationManager configurationManager;


    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }


    public void setConfigurationManager( final ConfigurationManager configurationManager )
    {
        this.configurationManager = configurationManager;
    }


    protected Object doExecute()
    {
        boolean result = configurationManager
                .injectConfiguration( hostname, pathToFile, json, ConfiguraitonTypeEnum.valueOf( type ) );

        LOG.info( "EchoTextToFileCommand@doExecute: " + result, result );
        return null;
    }
}
