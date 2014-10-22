package org.safehaus.subutai.plugin.sqoop.impl.handler;


import java.util.Arrays;
import java.util.HashSet;

import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.sqoop.api.setting.ImportSetting;
import org.safehaus.subutai.plugin.sqoop.impl.CommandFactory;
import org.safehaus.subutai.plugin.sqoop.impl.CommandType;
import org.safehaus.subutai.plugin.sqoop.impl.SqoopImpl;


public class ImportHandler extends AbstractHandler
{

    private ImportSetting settings;


    public ImportHandler( SqoopImpl manager, String clusterName, TrackerOperation po )
    {
        super( manager, clusterName, po );
    }


    public ImportSetting getSettings()
    {
        return settings;
    }


    public void setSettings( ImportSetting settings )
    {
        this.settings = settings;
        this.hostname = settings.getHostname();
    }


    @Override
    public void run()
    {
        TrackerOperation po = trackerOperation;
        Agent agent = manager.getAgentManager().getAgentByHostname( hostname );
        if ( agent == null )
        {
            po.addLogFailed( "Node is not connected" );
            return;
        }

        String s = CommandFactory.build( CommandType.IMPORT, settings );
        Command cmd = manager.getCommandRunner().createCommand( new RequestBuilder( s ).withTimeout( 600 ),
                new HashSet<>( Arrays.asList( agent ) ) );

        manager.getCommandRunner().runCommand( cmd );

        AgentResult res = cmd.getResults().get( agent.getUuid() );
        if ( cmd.hasSucceeded() )
        {
            po.addLog( res.getStdOut() );
            po.addLogDone( "Import completed on " + hostname );
        }
        else
        {
            po.addLog( res.getStdOut() );
            po.addLogFailed( res.getStdErr() );
        }
    }
}
