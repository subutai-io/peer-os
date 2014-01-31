package org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class CommandAction implements Action {

    private final String COMMAND_LINE;
    private final ActionListener ACTION_LISTENER;

    private Chain chain;
    private Context context;

    public CommandAction(String commandLine, ActionListener actionListener) {
        COMMAND_LINE = commandLine;
        ACTION_LISTENER = actionListener;
    }

    @Override
    public void execute(Context context, Chain chain) {
        ACTION_LISTENER.onExecute(context, COMMAND_LINE);
        reset(context, chain);
        CommandExecutor.INSTANCE.execute( CommandBuilder.getCommand(context, COMMAND_LINE), this);
    }

    private void reset(Context context, Chain chain) {
        this.chain = chain;
        this.context = context;
    }

    public void handleResponse(String stdOut, String stdErr, Response response) {

        boolean canContinue = ACTION_LISTENER.onResponse(context, stdOut, stdErr, response);

        if (canContinue && chain != null) {
            chain.proceed(context);
        }
    }
}
