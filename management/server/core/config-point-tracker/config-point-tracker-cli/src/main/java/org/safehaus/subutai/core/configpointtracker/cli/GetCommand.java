package org.safehaus.subutai.core.configpointtracker.cli;


import org.safehaus.subutai.core.configpointtracker.api.ConfigPointTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command( scope = "config-point-tracker", name = "get" )
public class GetCommand extends OsgiCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( GetCommand.class );
    @Argument( index = 0, name = "templateName", required = true )
    private String templateName;

    private ConfigPointTracker configPointTracker;


    public void setConfigPointTracker( ConfigPointTracker configPointTracker )
    {
        Preconditions.checkNotNull( configPointTracker, "ConfigPointTracker is null." );
        this.configPointTracker = configPointTracker;
    }


    protected Object doExecute()
    {
        LOG.info( "Result: " + configPointTracker.get( templateName ) );
        return null;
    }
}
