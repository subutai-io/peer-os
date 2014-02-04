package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;

public class ChainManager {

    protected UILogger logger;
    protected Action initAction;

    public ChainManager(UILogger logger) {
        this.logger = logger;
        initAction = new InitAction(logger);
    }

    public static void run(Chain chain) {
        chain.start(new Context());
    }
}
