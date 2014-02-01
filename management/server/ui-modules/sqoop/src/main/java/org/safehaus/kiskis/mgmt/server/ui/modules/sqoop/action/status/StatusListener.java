package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.status;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.AbstractListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UIStateManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class StatusListener extends AbstractListener {

    public StatusListener() {
        super("Checking status, please wait...");
    }
    
    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-hadoop")) {
            UILogger.info("Hadoop NOT INSTALLED. Please install hadoop before installing Sqoop.");
            UIStateManager.end();
            return false;
        }

        UILogger.info("Hadoop installed - OK");

        String msg = stdOut.contains("ksks-sqoop") ? "Sqoop installed - OK" : "Sqoop NOT INSTALLED";
        UILogger.info(msg);

        UIStateManager.end();

        return false;
    }

}
