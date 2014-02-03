package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action;

import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Set;

public class InitAction implements Action {

    private UILogger logger;

    public InitAction(UILogger logger) {
        this.logger = logger;
    }

    @Override
    public void execute(Context context, Chain chain) {

        Set<Agent> agents = MgmtApplication.getSelectedAgents();
        logger.clear();

        if (agents == null || agents.isEmpty()) {
            logger.info("Please select a node");
        } else if (agents.size() > 1) {
            logger.info("Please select a one node only");
        } else {
            proceed(context, chain, agents.iterator().next());
        }
    }

    private void proceed(Context context, Chain chain, Agent agent) {

        logger.info("Selected node: " + agent.getHostname());

        context.put("agent", agent);
        chain.proceed(context);
    }
}
