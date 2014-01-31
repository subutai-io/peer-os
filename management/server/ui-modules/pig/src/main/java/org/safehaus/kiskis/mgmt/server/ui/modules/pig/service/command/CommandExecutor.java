package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command;

import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;

public class CommandExecutor implements TaskCallback {

    private static final TaskRunner TASK_RUNNER = new TaskRunner();

    private StringBuilder stdOut;
    private StringBuilder stdErr;
    private CommandAction commandAction;

    public static final CommandExecutor INSTANCE = new CommandExecutor();

    public void execute(Command command, CommandAction commandAction) {
        reset(commandAction);
        execute(command);
    }

    private void reset(CommandAction commandAction) {
        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
        this.commandAction = commandAction;
    }

    private void execute(Command command) {
        Task task = new Task();
        task.addCommand(command);

        TASK_RUNNER.runTask(task, INSTANCE);
    }

    @Override
    public void onResponse(Task task, Response response) {
        if (response == null && response.getUuid() == null) {
            return;
        }

        if (!Util.isStringEmpty(response.getStdOut())) {
            stdOut.append(response.getStdOut());
        }

        if (!Util.isStringEmpty(response.getStdErr())) {
            stdErr.append(response.getStdErr());
        }

        if (Util.isFinalResponse(response)) {
            commandAction.handleResponse(stdOut.toString(), stdErr.toString(), response.getType());
        }
    }

    public void onResponse(Response response) {
        TASK_RUNNER.feedResponse(response);
    }
}

