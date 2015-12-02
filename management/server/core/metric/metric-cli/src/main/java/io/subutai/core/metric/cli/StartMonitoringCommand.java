package io.subutai.core.metric.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Environment;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitoringSettings;


/**
 * Start monitoring command
 */
@Command( scope = "monitoring", name = "start", description = "Starts monitoring environment" )
public class StartMonitoringCommand extends SubutaiShellCommandSupport
{
    private final Monitor monitor;
    private final EnvironmentManager environmentManager;

    @Argument( index = 0, name = "subscriberId", multiValued = false, description = "Subscriber ID" )
    protected String subscriberId;

    @Argument( index = 1, name = "environmentId", multiValued = false, description = "Environment ID" )
    protected String environmentId;


    public StartMonitoringCommand( final Monitor monitor, final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( monitor, "Monitor is null" );

        this.monitor = monitor;
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        Environment environment = environmentManager.loadEnvironment( environmentId );
        monitor.startMonitoring( subscriberId, environment, new MonitoringSettings() );
        return null;
    }
}
