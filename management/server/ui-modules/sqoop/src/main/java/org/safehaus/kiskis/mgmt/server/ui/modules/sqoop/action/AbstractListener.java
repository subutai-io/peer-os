package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;

public abstract class AbstractListener extends ActionListener {

    protected final String EXECUTE_MESSAGE;

    protected AbstractListener(String executeMessage) {
        EXECUTE_MESSAGE = executeMessage;
    }

    @Override
    public void onExecute(Context context, String programLine) {
        UILogger.info(EXECUTE_MESSAGE);
    }
}
