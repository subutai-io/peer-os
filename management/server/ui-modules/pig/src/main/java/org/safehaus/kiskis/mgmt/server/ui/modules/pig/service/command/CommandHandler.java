package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.action.Action;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.logging.Logger;

public class CommandHandler implements TaskCallback {

    private static final TaskRunner TASK_RUNNER = new TaskRunner();

    private static StringBuilder stdOut;
    private static StringBuilder stdErr;
    private static Action action;

    public static final CommandHandler INSTANCE = new CommandHandler();

    public static void handle(Command command, Action action) {
        reset(action);
        runCommand(command);
    }

    private static void reset(Action _action) {
        action = _action;
        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
    }

    private static void runCommand(Command command) {
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
            action.handleResponse(stdOut.toString(), stdErr.toString(), response.getType());
        }
    }

    public void onCommand(Response response) {
        TASK_RUNNER.feedResponse(response);
    }
}

