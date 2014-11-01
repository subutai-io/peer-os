package org.safehaus.subutai.plugin.hadoop.impl.common;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


/**
 * Created by daralbaev on 22.04.14.
 */
public class AddNodeOperation
{
    private final HadoopClusterConfig hadoopClusterConfig;
    private List<Command> commandList;
    private Agent agent;


    public AddNodeOperation( Commands commands, HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {

        this.hadoopClusterConfig = hadoopClusterConfig;
        this.agent = agent;
        commandList = new ArrayList<>();

        //		commandList.add(Commands.getInstallCommand(agent));
//        commandList.add( commands.getSetMastersCommand( hadoopClusterConfig, agent ) );
//        commandList.add( commands.getExcludeDataNodeCommand( hadoopClusterConfig, agent ) );
//        commandList.add( commands.getExcludeTaskTrackerCommand( hadoopClusterConfig, agent ) );
//        commandList.add( commands.getSetDataNodeCommand( hadoopClusterConfig, agent ) );
//        commandList.add( commands.getSetTaskTrackerCommand( hadoopClusterConfig, agent ) );
//        commandList.add( commands.getStartDataNodeCommand( agent ) );
//        commandList.add( commands.getStartTaskTrackerCommand( agent ) );
    }


    public List<Command> getCommandList()
    {
        return commandList;
    }
}
