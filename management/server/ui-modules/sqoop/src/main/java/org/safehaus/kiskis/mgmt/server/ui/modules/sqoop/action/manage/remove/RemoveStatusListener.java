package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.remove;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.AbstractListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class RemoveStatusListener extends AbstractListener {

    public RemoveStatusListener(UILogger logger) {
        super(logger, "Checking status, please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-sqoop")) {
            logger.complete("Sqoop NOT INSTALLED. Nothing to remove.");
            return false;
        }

        return true;
    }

}
