package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.remove;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class RemoveListener extends ActionListener {

    private UILogger log;

    public RemoveListener(UILogger log) {
        this.log = log;
    }

    @Override
    public void onExecute(Context context, String programLine) {
        log.info("Pig installed - removing, please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (response.getExitCode() == null || response.getExitCode() == 0) {
            log.info("Pig successfully removed");
        } else {
            log.info("Error occurred while removing Pig. Please see the server logs for details.");
        }

        log.info("Completed");

        return false;
    }

}
