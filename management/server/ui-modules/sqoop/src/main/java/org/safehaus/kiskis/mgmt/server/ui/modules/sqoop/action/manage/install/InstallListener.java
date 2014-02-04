package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallListener extends BasicListener {

    public InstallListener(UILogger logger) {
        super(logger, "Installing Sqoop, please wait...");
    }

    @Override
    protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

        String msg = response.getExitCode() == null || response.getExitCode() == 0
                ? "Sqoop installed successfully"
                : "Error occurred while installing Sqoop. Please see the server logs for details.";

        logger.complete(msg);

        return false;
    }

}
