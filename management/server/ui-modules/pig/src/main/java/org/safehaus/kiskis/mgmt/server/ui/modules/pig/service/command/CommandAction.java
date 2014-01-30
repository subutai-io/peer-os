package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command;

import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.BaseAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Chain;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class CommandAction extends BaseAction implements ResponseHandler {

    private final Logger LOG = Logger.getLogger(getClass().getName());
    private Chain chain;
    private Map<String, Object> context;

    private String commandString = "dpkg -l|grep ksks";

    public void execute(Map<String, Object> context, Chain chain) {
        this.chain = chain;
        this.context = context;

        Command cmd = CommandBuilder.getTemplate();

        Agent agent = (Agent) context.get("agent");
        cmd.getRequest().setUuid(agent.getUuid());

        cmd.getRequest().setProgram(commandString);

        CommandExecutor.execute(cmd, this);
    }

    @Override
    public void handleResponse(String stdOut, String stdErr, ResponseType responseType) {
        LOG.info(">> " + stdOut);
        //chain.execute(context);
    }
}
