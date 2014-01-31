package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.AbstractListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallListener extends AbstractListener {

    public InstallListener(UILogger log) {
        super(log, "Installing Pig, please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        String msg = response.getExitCode() == null || response.getExitCode() == 0
                ? "Pig installed successfully"
                : "Error occurred while installing Pig. Please see the server logs for details.";

        LOG.info(msg);
        LOG.info("Completed");

        return false;
    }

}
