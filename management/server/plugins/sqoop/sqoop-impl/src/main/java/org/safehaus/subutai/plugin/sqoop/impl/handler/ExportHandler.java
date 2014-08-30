package org.safehaus.subutai.plugin.sqoop.impl.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.plugin.sqoop.api.setting.ExportSetting;
import org.safehaus.subutai.plugin.sqoop.impl.CommandFactory;
import org.safehaus.subutai.plugin.sqoop.impl.CommandType;
import org.safehaus.subutai.plugin.sqoop.impl.SqoopImpl;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

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

    @Override
    public void run() {
        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed("Node is not connected");
            return;
        }

        String s = CommandFactory.build(CommandType.EXPORT, settings);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s).withTimeout(60),
                new HashSet<>(Arrays.asList(agent)));

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
