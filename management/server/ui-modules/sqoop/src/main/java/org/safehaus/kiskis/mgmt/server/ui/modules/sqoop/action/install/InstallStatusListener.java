package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.AbstractListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UIStateManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallStatusListener extends AbstractListener {

    public InstallStatusListener() {
        super("Checking status, please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-hadoop")) {
            UILogger.info("Hadoop NOT INSTALLED. Please install Hadoop before installing Sqoop.");
            UIStateManager.end();
            return false;
        }

        UILogger.info("Hadoop installed - OK");

        if (stdOut.contains("ksks-sqoop")) {
            UILogger.info("Sqoop ALREADY INSTALLED");
            UIStateManager.end();
            return false;
        }

        return true;
    }

}
