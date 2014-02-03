package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;

public abstract class AbstractListener extends ActionListener {

    protected UILogger logger;
    protected String executeMessage;

    protected AbstractListener(UILogger logger, String executeMessage) {
        this.logger = logger;
        this.executeMessage = executeMessage;
    }

    @Override
    public void onExecute(Context context, String programLine) {
        logger.info(executeMessage);
    }
}
