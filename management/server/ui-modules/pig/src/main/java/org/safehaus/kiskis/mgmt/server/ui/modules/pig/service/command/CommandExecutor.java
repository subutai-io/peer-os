package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command;

import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;

public class CommandExecutor implements TaskCallback {

    private static final TaskRunner TASK_RUNNER = new TaskRunner();

    private static StringBuilder stdOut;
    private static StringBuilder stdErr;
    private static ResponseHandler responseHandler;

    public static final CommandExecutor INSTANCE = new CommandExecutor();

    public static void execute(Command command, ResponseHandler responseHandler) {
        reset(responseHandler);
        runCommand(command);
    }

    private static void reset(ResponseHandler _responseHandler) {
        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
        responseHandler = _responseHandler;
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
            responseHandler.handleResponse(stdOut.toString(), stdErr.toString(), response.getType());
        }
    }

    public void onResponse(Response response) {
        TASK_RUNNER.feedResponse(response);
    }
}

