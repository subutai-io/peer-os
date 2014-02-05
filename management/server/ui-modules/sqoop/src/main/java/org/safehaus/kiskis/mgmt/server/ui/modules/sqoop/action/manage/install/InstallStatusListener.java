package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallStatusListener extends BasicListener {

    public InstallStatusListener(UILogger logger) {
        super(logger, "Checking status, please wait...");
    }

    @Override
    protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-hadoop")) {
            logger.complete("Hadoop NOT INSTALLED. Please install Hadoop before installing Sqoop.");
            return false;
        }

        logger.info("Hadoop installed - OK");

        if (stdOut.contains("ksks-sqoop")) {
            logger.complete("Sqoop ALREADY INSTALLED");
            return false;
        }

        return true;
    }

}
