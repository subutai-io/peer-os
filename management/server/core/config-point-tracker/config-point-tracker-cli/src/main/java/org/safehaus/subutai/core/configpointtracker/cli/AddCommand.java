package org.safehaus.subutai.core.configpointtracker.cli;


import org.safehaus.subutai.core.configpointtracker.api.ConfigPointTracker;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command(scope = "config-point-tracker", name = "add")
public class AddCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "templateName", required = true)
    private String templateName;

    @Argument(index = 1, name = "configPath", required = true)
    private String configPath;

    private ConfigPointTracker configPointTracker;


    public void setConfigPointTracker( ConfigPointTracker configPointTracker )
    {
        Preconditions.checkNotNull( configPointTracker, "TemplateRegistry is null." );
        this.configPointTracker = configPointTracker;
    }


    protected Object doExecute()
    {

        configPointTracker.add( templateName, configPath );

        return null;
    }
}
