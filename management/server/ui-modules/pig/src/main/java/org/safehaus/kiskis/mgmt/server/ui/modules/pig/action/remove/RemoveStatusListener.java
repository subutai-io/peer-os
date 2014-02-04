package org.safehaus.kiskis.mgmt.server.ui.modules.pig.action.remove;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.action.AbstractListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.view.UIStateManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class RemoveStatusListener extends AbstractListener {

    public RemoveStatusListener() {
        super("Checking status, please wait...");
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-pig")) {
            UILogger.info("Pig NOT INSTALLED. Nothing to remove.");
            UIStateManager.end();
            return false;
        }

        return true;
    }

}
