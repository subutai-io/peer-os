package org.safehaus.subutai.plugin.hadoop.impl.common;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public class InstallHadoopOperation
{
    private final HadoopClusterConfig hadoopClusterConfig;
    private List<Command> commandList;


    public InstallHadoopOperation( Commands commands, HadoopClusterConfig hadoopClusterConfig )
    {

        this.hadoopClusterConfig = hadoopClusterConfig;
        commandList = new ArrayList<>();

        //		commandList.add(commands.getInstallCommand(config));
        //        commandList.add( commands.getClearMastersCommand( hadoopClusterConfig ) );
        //        commandList.add( commands.getClearSlavesCommand( hadoopClusterConfig ) );
        //        commandList.add( commands.getSetMastersCommand( hadoopClusterConfig ) );
        //        commandList.add( commands.getAddSecondaryNamenodeCommand( hadoopClusterConfig ) );
        //        commandList.add( commands.getSetDataNodeCommand( hadoopClusterConfig ) );
        //        commandList.add( commands.getSetTaskTrackerCommand( hadoopClusterConfig ) );
        //        commandList.add( commands.getFormatNameNodeCommand( hadoopClusterConfig ) );
    }


    public List<Command> getCommandList()
    {
        return commandList;
    }
}
