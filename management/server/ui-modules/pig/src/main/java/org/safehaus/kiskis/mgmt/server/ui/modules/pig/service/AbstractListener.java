package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.ActionListener;

public abstract class AbstractListener extends ActionListener {

    protected final UILogger LOG;
    protected final String EXECUTE_MESSAGE;

    protected AbstractListener(UILogger log, String executeMessage) {
        LOG = log;
        EXECUTE_MESSAGE = executeMessage;
    }

    @Override
    public void onExecute(Context context, String programLine) {
        LOG.info(EXECUTE_MESSAGE);
    }
}
