package org.safehaus.subutai.core.configuration.cli;


import org.safehaus.subutai.core.configuration.api.TextInjector;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "config", name = "get-config-template",
        description = "Gets the content of given configuration tempalte")
public class GetConfigTemplateCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "pathToFile", required = true, multiValued = false, description = "Path to file")
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

        String fileContent = textInjector.getConfigTemplate( pathToFile );
        System.out.println( fileContent );

        return null;
    }
}
