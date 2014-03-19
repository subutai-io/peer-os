package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command;

import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.Sqoop;
import org.safehaus.kiskis.mgmt.shared.protocol.*;

public class CommandExecutor implements TaskCallback {

    private StringBuilder stdOut;
    private StringBuilder stdErr;
    private CommandAction commandAction;

    public static final CommandExecutor INSTANCE = new CommandExecutor();

    public void execute(Request command, CommandAction commandAction) {
        reset(commandAction);
        execute(command);
    }

    private void reset(CommandAction commandAction) {
        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
        this.commandAction = commandAction;
    }

    private void execute(Request command) {
        Task task = new Task();
        task.addRequest(command);

        Sqoop.getTaskRunner().executeTask(task, INSTANCE);
    }

    @Override
    public Task onResponse(Task task, Response response, String stdOut2, String stdErr2) {
        if (response == null || response.getUuid() == null) {
            return null;
        }

        if (!Util.isStringEmpty(response.getStdOut())) {
            stdOut.append(response.getStdOut());
        }

        if (!Util.isStringEmpty(response.getStdErr())) {
            stdErr.append(response.getStdErr());
        }

        commandAction.onResponse(response);

        if (Util.isFinalResponse(response)) {
            commandAction.onComplete(stdOut.toString(), stdErr.toString(), response);
        }

        return null;
    }
}
