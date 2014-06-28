package org.safehaus.subutai.impl.template;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.shared.protocol.Agent;

class ScriptExecutor {

    private final CommandRunner commandRunner;

    public ScriptExecutor(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public boolean execute(Agent agent, ActionType actionType, String... args) {

        if(agent == null || actionType == null) return false;

        Command cmd = commandRunner.createCommand(
                new RequestBuilder(actionType.buildCommand(args)),
                new HashSet<>(Arrays.asList(agent)));

        commandRunner.runCommand(cmd);
        return cmd.hasSucceeded();
    }
}
