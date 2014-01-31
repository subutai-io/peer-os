package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.AbstractListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallStatusListener extends AbstractListener {

    public InstallStatusListener(UILogger log) {
        super(log, "Checking status, please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-hadoop")) {
            LOG.info("Hadoop NOT INSTALLED. Please install hadoop before installing Pig.");
            LOG.info("Completed");
            return false;
        }

        LOG.info("Hadoop installed - OK");

        if (stdOut.contains("ksks-pig")) {
            LOG.info("Pig ALREADY INSTALLED");
            LOG.info("Completed");
            return false;
        }

        return true;
    }

}
