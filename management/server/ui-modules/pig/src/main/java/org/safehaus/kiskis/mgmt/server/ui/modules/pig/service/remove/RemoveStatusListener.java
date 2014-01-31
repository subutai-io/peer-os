package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.remove;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.AbstractListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class RemoveStatusListener extends AbstractListener {

    public RemoveStatusListener(UILogger log) {
        super(log, "Checking status, please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-pig")) {
            LOG.info("Pig NOT INSTALLED. Nothing to remove.");
            LOG.info("Completed");
            return false;
        }

        return true;
    }

}
