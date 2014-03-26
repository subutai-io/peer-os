package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicListener extends ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(BasicListener.class);

    protected UILogger logger;
    protected String executeMessage;

    protected BasicListener(UILogger logger, String executeMessage) {
        this.logger = logger;
        this.executeMessage = executeMessage;
    }

    @Override
    protected void onStart(Context context, String programLine) {
        LOG.info(executeMessage);
        logger.info(executeMessage);
    }

    @Override
    protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

        LOG.info("response: {}", response);
        LOG.info("stdOut: {}", stdOut);
        LOG.info("stdErr: {}", stdErr);

        String msg = response.getExitCode() == null || response.getExitCode() == 0
                ? "Import performed successfully"
                : "Error occurred. Please see the server logs for details.";

        logger.complete(msg);

        return false;
    }
}
