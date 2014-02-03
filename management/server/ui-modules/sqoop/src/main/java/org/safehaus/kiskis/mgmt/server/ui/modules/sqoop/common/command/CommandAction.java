package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class CommandAction implements Action {

    private String commandLine;
    private ActionListener actionListener;

    private Chain chain;
    private Context context;

    public CommandAction(String commandLine, ActionListener actionListener) {
        this.commandLine = commandLine;
        this.actionListener = actionListener;
    }

    @Override
    public void execute(Context context, Chain chain) {
        actionListener.onStart(context, commandLine);
        reset(context, chain);
        CommandExecutor.INSTANCE.execute( CommandBuilder.getCommand(context, commandLine), this);
    }

    private void reset(Context context, Chain chain) {
        this.chain = chain;
        this.context = context;
    }

    protected void onResponse(Response response) {
        actionListener.onResponse(context, response);
    }

    protected void onComplete(String stdOut, String stdErr, Response response) {

        boolean canContinue = actionListener.onComplete(context, stdOut, stdErr, response);

        if (canContinue && chain != null) {
            chain.proceed(context);
        }
    }
}
