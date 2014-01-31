package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.view.UILog;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.logging.Logger;

public class InstallListener extends ActionListener {

    private UILog log;

    public InstallListener(UILog log) {
        this.log = log;
    }

    @Override
    public void onExecute(Context context, String programLine) {
        log.log("Installing Pig. Please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (response.getExitCode() == null || response.getExitCode() == 0) {
            log.log("Pig successfully installed");
        } else {
            log.log("Error occurred while installing Pig. Please see the server logs for details.");
        }

        log.log("Completed");

        return false;
    }

}
