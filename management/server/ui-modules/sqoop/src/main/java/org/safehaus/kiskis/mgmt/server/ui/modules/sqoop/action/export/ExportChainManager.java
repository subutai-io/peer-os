package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.export;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.status.StatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;

public class ExportChainManager extends ChainManager {

    private static final String STATUS_COMMAND = "dpkg -l|grep ksks";

    public ExportChainManager(UILogger logger) {
        super(logger);
    }
    public Chain getStatusChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new ExportListener(logger));

        return new Chain(initAction, statusAction);
    }
}
