package org.safehaus.subutai.plugin.hadoop.impl.common;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


/**
 * Created by daralbaev on 22.04.14.
 */
public class AddNodeOperation {
    private final HadoopClusterConfig hadoopClusterConfig;
    private List<Command> commandList;
    private Agent agent;


    public AddNodeOperation( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {

        this.hadoopClusterConfig = hadoopClusterConfig;
        this.agent = agent;
        commandList = new ArrayList<>();

        //		commandList.add(Commands.getInstallCommand(agent));
        commandList.add( Commands.getSetMastersCommand( hadoopClusterConfig, agent ) );
        commandList.add( Commands.getExcludeDataNodeCommand( hadoopClusterConfig, agent ) );
        commandList.add( Commands.getExcludeTaskTrackerCommand( hadoopClusterConfig, agent ) );
        commandList.add( Commands.getSetDataNodeCommand( hadoopClusterConfig, agent ) );
        commandList.add( Commands.getSetTaskTrackerCommand( hadoopClusterConfig, agent ) );
        commandList.add( Commands.getStartNameNodeCommand( agent ) );
        commandList.add( Commands.getStartTaskTrackerCommand( agent ) );
    }


    public List<Command> getCommandList() {
        return commandList;
    }
}
