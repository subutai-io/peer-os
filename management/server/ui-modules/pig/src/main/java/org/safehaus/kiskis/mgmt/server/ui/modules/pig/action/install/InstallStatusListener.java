package org.safehaus.kiskis.mgmt.server.ui.modules.pig.action.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.action.AbstractListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.view.UIStateManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallStatusListener extends AbstractListener {

    public InstallStatusListener() {
        super("Checking status, please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-hadoop")) {
            UILogger.info("Hadoop NOT INSTALLED. Please install hadoop before installing Pig.");
            UIStateManager.end();
            return false;
        }

        LOG.info("Hadoop installed - OK");

        if (stdOut.contains("ksks-pig")) {
            UILogger.info("Pig ALREADY INSTALLED");
            UIStateManager.end();
            return false;
        }

        return true;
    }

}
