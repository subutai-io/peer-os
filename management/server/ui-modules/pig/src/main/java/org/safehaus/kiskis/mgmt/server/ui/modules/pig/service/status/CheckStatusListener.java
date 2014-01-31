package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.status;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class CheckStatusListener extends ActionListener {

    private UILogger log;

    public CheckStatusListener(UILogger log) {
        this.log = log;
    }

    @Override
    public void onExecute(Context context, String programLine) {

        Agent agent = context.get("agent");

        log.clear();
        log.info("Checking status for %s. Please wait...", agent.getHostname());
    }

    @Override
    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        if (stdOut == null || !stdOut.contains("ksks-hadoop")) {
            log.info("Hadoop NOT INSTALLED. Please install hadoop before installing Pig.");
            log.info("Completed");
            return false;
        }

        log.info("Hadoop installed - OK");

        if (stdOut.contains("ksks-pig")) {
            log.info("Pig installed - OK");
        } else {
            log.info("Pig NOT INSTALLED");
        }

        log.info("Completed");

        return false;
    }

}
