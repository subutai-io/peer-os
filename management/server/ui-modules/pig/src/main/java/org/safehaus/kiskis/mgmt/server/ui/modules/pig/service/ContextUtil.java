package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service;

import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Set;

public class ContextUtil {

    public static Context create() {

        Set<Agent> agents = MgmtApplication.getSelectedAgents();
        Agent agent = null;

        for (Agent a : agents) {
            agent = a;
        }

        Context context = new Context();
        context.put("agent", agent);

        return context;
    }
}
