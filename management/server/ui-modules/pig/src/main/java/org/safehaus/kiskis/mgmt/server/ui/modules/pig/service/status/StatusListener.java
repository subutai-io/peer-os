package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.status;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.AbstractListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class StatusListener extends AbstractListener {

    public StatusListener(UILogger log) {
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

        String msg = stdOut.contains("ksks-pig") ? "Pig installed - OK" : "Pig NOT INSTALLED";
        LOG.info(msg);

        LOG.info("Completed");

        return false;
    }

}
