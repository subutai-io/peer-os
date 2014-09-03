package org.safehaus.subutai.plugin.sqoop.impl.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.impl.CommandFactory;
import org.safehaus.subutai.plugin.sqoop.impl.CommandType;
import org.safehaus.subutai.plugin.sqoop.impl.SqoopImpl;

public class CheckHandler extends AbstractHandler {

    public CheckHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        SqoopConfig config = getClusterConfig();
        if(config == null) {
            po.addLogFailed("Sqoop installation not found: " + clusterName);
            return;
        }
        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed("Node is not connected");
            return;
        }

        String s = CommandFactory.build(CommandType.LIST, null);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s),
                new HashSet<>(Arrays.asList(agent)));

        manager.getCommandRunner().runCommand(cmd);

        AgentResult res = cmd.getResults().get(agent.getUuid());
        if(cmd.hasSucceeded())
            if(res.getStdOut().contains(CommandFactory.PACKAGE_NAME))
                po.addLogDone("Sqoop installed on " + hostname);
            else
                po.addLogFailed("Sqoop not installed on " + hostname);
        else {
            po.addLog(res.getStdOut());
            po.addLog(res.getStdErr());
            po.addLogFailed("Failed to check Sqoop");
        }

    }

}
