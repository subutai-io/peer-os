package org.safehaus.kiskis.mgmt.impl.sqoop.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.sqoop.setting.ExportSetting;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.sqoop.CommandFactory;
import org.safehaus.kiskis.mgmt.impl.sqoop.CommandType;
import org.safehaus.kiskis.mgmt.impl.sqoop.SqoopImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class ExportHandler extends AbstractHandler {

    private ExportSetting settings;

    public ExportHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public ExportSetting getSettings() {
        return settings;
    }

    public void setSettings(ExportSetting settings) {
        this.settings = settings;
        this.hostname = settings.getHostname();
    }

    public void run() {
        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed("Node is not connected");
            return;
        }

        String s = CommandFactory.build(CommandType.EXPORT, settings);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s).withTimeout(60),
                new HashSet<Agent>(Arrays.asList(agent)));

        manager.getCommandRunner().runCommand(cmd);

        AgentResult res = cmd.getResults().get(agent.getUuid());
        if(cmd.hasSucceeded()) {
            po.addLog(res.getStdOut());
            po.addLogDone("Export completed on " + hostname);
        } else {
            po.addLog(res.getStdOut());
            po.addLogFailed(res.getStdErr());
        }
    }

}
