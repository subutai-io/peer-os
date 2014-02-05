package org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.status;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallStatusListener extends BasicListener {

    public InstallStatusListener(UILogger logger) {
        super(logger, "Checking installation status, please wait...");
    }

    @Override
    protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-hadoop")) {
            logger.complete("Hadoop NOT INSTALLED. Please install hadoop before installing Hive.");
            return false;
        }

        logger.info("Hadoop installed - OK");

        if (!stdOut.contains("ksks-hive")) {
            logger.complete("Hive NOT INSTALLED");
            return false;
        }

        String msg = stdOut.contains("ksks-derby") ? "Derby installed - OK" : "Derby NOT INSTALLED";
        logger.info(msg);

        return true;
    }
}
