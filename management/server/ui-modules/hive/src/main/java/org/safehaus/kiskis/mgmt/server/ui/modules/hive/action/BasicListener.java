package org.safehaus.kiskis.mgmt.server.ui.modules.hive.action;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class BasicListener extends ActionListener {

    protected UILogger logger;
    protected String executeMessage;

    protected BasicListener(UILogger logger, String executeMessage) {
        this.logger = logger;
        this.executeMessage = executeMessage;
    }

    @Override
    protected void onStart(Context context, String programLine) {
        logger.info(executeMessage);
    }

    @Override
    protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

        logger.info(stdOut);
        logger.info(stdErr);
        logger.info("Completed");

        return false;
    }
}
