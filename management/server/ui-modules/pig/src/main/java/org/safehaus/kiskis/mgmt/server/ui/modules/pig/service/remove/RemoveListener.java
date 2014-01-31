package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.remove;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.view.UILog;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class RemoveListener extends ActionListener {

    private UILog log;

    public RemoveListener(UILog log) {
        this.log = log;
    }

    @Override
    public void onExecute(Context context, String programLine) {
        log.log("Pig installed. Removing, please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (response.getExitCode() == null || response.getExitCode() == 0) {
            log.log("Pig successfully removed");
        } else {
            log.log("Error occurred while removing Pig. Please see the server logs for details.");
        }

        log.log("Completed");

        return false;
    }

}
