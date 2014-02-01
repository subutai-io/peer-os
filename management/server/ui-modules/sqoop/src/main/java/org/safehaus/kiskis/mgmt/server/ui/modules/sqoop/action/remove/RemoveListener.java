package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.remove;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.AbstractListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UIStateManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class RemoveListener extends AbstractListener {

    public RemoveListener() {
        super("Pig installed. Removing, please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        String msg = response.getExitCode() == null || response.getExitCode() == 0
                ? "Pig removed successfully"
                : "Error occurred while removing Pig. Please see the server logs for details.";

        UILogger.info(msg);
        UIStateManager.end();

        return false;
    }

}
