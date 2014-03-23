package org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command;

import org.apache.commons.lang3.StringUtils;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.Hive;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

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

        Hive.getTaskRunner().executeTask(task, INSTANCE);
    }

    @Override
    public Task onResponse(Task task, Response response, String stdOut2, String stdErr2) {
        if (response == null || response.getUuid() == null) {
            return null;
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

        return null;
    }

}
