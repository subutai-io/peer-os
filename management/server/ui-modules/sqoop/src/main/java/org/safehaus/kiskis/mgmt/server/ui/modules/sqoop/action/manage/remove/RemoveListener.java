package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.remove;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class RemoveListener extends BasicListener {

    public RemoveListener(UILogger logger) {
        super(logger, "Sqoop installed. Removing, please wait...");
    }

    @Override
    protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

        String msg = response.getExitCode() == null || response.getExitCode() == 0
                ? "Sqoop removed successfully"
                : "Error occurred while removing Pig. Please see the server logs for details.";

        logger.complete(msg);

        return false;
    }

}
