package org.safehaus.subutai.core.configpointtracker.cli;


import org.safehaus.subutai.core.configpointtracker.api.ConfigPointTracker;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "config-point-tracker", name = "get" )
public class GetCommand extends OsgiCommandSupport
{

    @Argument( index = 0, name = "templateName", required = true )
    private String templateName;

    private ConfigPointTracker configPointTracker;


    public void setConfigPointTracker( ConfigPointTracker configPointTracker )
    {
        this.configPointTracker = configPointTracker;
    }


    protected Object doExecute()
    {

        System.out.println( "Result: " + configPointTracker.get( templateName ) );

        return null;
    }
}
