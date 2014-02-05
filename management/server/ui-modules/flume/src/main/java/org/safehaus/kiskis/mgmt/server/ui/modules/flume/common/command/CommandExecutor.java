package org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.command;

import org.apache.commons.lang3.StringUtils;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
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
        if (response == null || response.getUuid() == null) {
            return;
        }

        if (!StringUtils.isEmpty(response.getStdOut())) {
            stdOut.append(response.getStdOut());
        }

        if (!StringUtils.isEmpty(response.getStdErr())) {
            stdErr.append(response.getStdErr());
        }

        commandAction.onResponse(response);

        if (Util.isFinalResponse(response)) {
            commandAction.onComplete(stdOut.toString(), stdErr.toString(), response);
        }
    }

    public void onResponse(Response response) {
        TASK_RUNNER.feedResponse(response);
    }
}

