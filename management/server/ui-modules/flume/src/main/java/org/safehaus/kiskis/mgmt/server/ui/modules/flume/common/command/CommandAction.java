package org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.command;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class CommandAction implements Action {

    protected String commandLine;
    protected ActionListener actionListener;

    protected Context context;
    protected Chain chain;
    protected boolean substitute;

    public CommandAction(String commandLine, ActionListener actionListener) {
        this(commandLine, actionListener, false);
    }

    public CommandAction(String commandLine, ActionListener actionListener, boolean substitute) {
        this.commandLine = commandLine;
        this.actionListener = actionListener;
        this.substitute = substitute;
    }

    @Override
    public void execute(Context context, Chain chain) {

        ActionListener.Result result = actionListener.onStart(context, commandLine);

        switch (result) {
        case CONTINUE:
            reset(context, chain);
            CommandExecutor.INSTANCE.execute( CommandBuilder.getCommand(context, getCommandLine()), this);
            break;
        case SKIP:
            chain.proceed(context);
            break;
        }
    }

    protected String getCommandLine() {
        return substitute ? new StrSubstitutor(context).replace(commandLine) : commandLine;
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

        if (canContinue) {
            chain.proceed(context);
        }
    }
}
