package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.status;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class StatusListener extends BasicListener {

    public StatusListener(UILogger logger) {
        super(logger, "Checking status, please wait...");
    }

    @Override
    protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-hadoop")) {
            logger.complete("Hadoop NOT INSTALLED. Please install hadoop before installing Sqoop.");
            return false;
        }

        logger.info("Hadoop installed - OK");

        String msg = stdOut.contains("ksks-sqoop") ? "Sqoop installed - OK" : "Sqoop NOT INSTALLED";
        logger.complete(msg);

        return false;
    }
}
