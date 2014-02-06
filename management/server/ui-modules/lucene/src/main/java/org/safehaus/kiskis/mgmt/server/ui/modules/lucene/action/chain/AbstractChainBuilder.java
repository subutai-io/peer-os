package org.safehaus.kiskis.mgmt.server.ui.modules.lucene.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.action.AgentInitAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.view.UILogger;

public abstract class AbstractChainBuilder {

    protected static final String STATUS_COMMAND = "dpkg -l|grep ksks";

    protected  UILogger logger;
    protected  Action agentInitAction;

    protected AbstractChainBuilder(UILogger logger) {
        this.logger = logger;
        agentInitAction = new AgentInitAction(logger);
    }

    public abstract Chain getChain();
}
