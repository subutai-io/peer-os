package org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

public class CommandAction implements Action {

    private final String PROGRAM_LINE;
    private final ActionListener ACTION_LISTENER;

    private Chain chain;
    private Context context;

    public CommandAction(String programLine, ActionListener actionListener) {
        PROGRAM_LINE = programLine;
        ACTION_LISTENER = actionListener;
    }

    public void execute(Context context, Chain chain) {
        ACTION_LISTENER.onExecute(context, PROGRAM_LINE);
        reset(context, chain);
        CommandExecutor.INSTANCE.execute(getCommand(), this);
    }

    private void reset(Context context, Chain chain) {
        this.chain = chain;
        this.context = context;
    }

    // TODO
    private Command getCommand() {
        Command cmd = CommandBuilder.getTemplate();

        Agent agent = context.get("agent");
        cmd.getRequest().setUuid(agent.getUuid());

        cmd.getRequest().setProgram(PROGRAM_LINE);

        return cmd;
    }

    public void handleResponse(String stdOut, String stdErr, Response response) {

        boolean canContinue = ACTION_LISTENER.onResponse(context, stdOut, stdErr, response);

        if (canContinue && chain != null) {
            chain.execute(context);
        }
    }
}
