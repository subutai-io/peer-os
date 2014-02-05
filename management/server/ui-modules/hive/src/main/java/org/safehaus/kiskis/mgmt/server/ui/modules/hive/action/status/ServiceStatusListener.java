package org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.status;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class ServiceStatusListener extends BasicListener {

    public ServiceStatusListener(UILogger logger) {
        super(logger, "Checking service status, please wait...");
    }

    @Override
    protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
        logger.complete(stdOut);
        return false;
    }
}
