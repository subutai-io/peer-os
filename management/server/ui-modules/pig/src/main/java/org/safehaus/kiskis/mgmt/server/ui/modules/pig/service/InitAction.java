package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service;

import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Set;

public class InitAction implements Action {

    private final UILogger LOG;

    InitAction(UILogger log) {
        LOG = log;
    }

    @Override
    public void execute(Context context, Chain chain) {

        Set<Agent> agents = MgmtApplication.getSelectedAgents();
        LOG.clear();

        if (agents == null || agents.isEmpty()) {
            LOG.info("Please select a node");
        } else if (agents.size() > 1) {
            LOG.info("Please select a one node only");
        } else {
            proceed(context, chain, agents.iterator().next());
        }
    }

    private void proceed(Context context, Chain chain, Agent agent) {
        context.put("agent", agent);
        chain.proceed(context);
    }
}
