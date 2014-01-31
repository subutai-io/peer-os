package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.view.UILog;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

public class CheckListener extends ActionListener {

    private UILog log;

    public CheckListener(UILog log) {
        this.log = log;
    }

    @Override
    public void onExecute(Context context, String programLine) {

        Agent agent = context.get("agent");

        log.clear();
        log.log("Checking status for %s. Please wait...", agent.getHostname());
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-hadoop")) {
            log.log("Hadoop NOT INSTALLED. Please install hadoop before installing Pig.");
            log.log("Completed");
            return false;
        }

        log.log("Hadoop installed - OK");

        if (stdOut.contains("ksks-pig")) {
            log.log("Pig ALREADY INSTALLED");
            log.log("Completed");
            return false;
        }

        return true;
    }

}
