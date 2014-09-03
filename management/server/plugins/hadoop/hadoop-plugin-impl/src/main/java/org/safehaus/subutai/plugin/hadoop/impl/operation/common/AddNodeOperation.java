package org.safehaus.subutai.plugin.hadoop.impl.operation.common;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.Commands;


public class AddNodeOperation {
    private List<Command> commandList;


    public AddNodeOperation( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {

        commandList = new ArrayList<>();

        commandList.add( Commands.getInstallCommand( agent ) );
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
