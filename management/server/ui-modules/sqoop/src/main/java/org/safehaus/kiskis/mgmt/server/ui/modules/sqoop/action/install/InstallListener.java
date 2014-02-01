package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.AbstractListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UIStateManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallListener extends AbstractListener {

    public InstallListener() {
        super("Installing Pig, please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        String msg = response.getExitCode() == null || response.getExitCode() == 0
                ? "Pig installed successfully"
                : "Error occurred while installing Pig. Please see the server logs for details.";

        UILogger.info(msg);
        UIStateManager.end();

        return false;
    }

}
