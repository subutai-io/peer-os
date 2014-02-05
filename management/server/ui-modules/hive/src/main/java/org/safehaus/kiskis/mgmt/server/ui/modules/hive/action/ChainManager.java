package org.safehaus.kiskis.mgmt.server.ui.modules.hive.action;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.status.InstallStatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.status.ServiceStatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.CommandAction;

public class ChainManager {

    private static final String STATUS_COMMAND = "dpkg -l|grep ksks";
    private static final String SERVICE_COMMAND = "service hive-thrift status";
//    private static final String INSTALL_COMMAND = "apt-get --force-yes --assume-yes install ksks-sqoop";
//    private static final String REMOVE_COMMAND = "apt-get --force-yes --assume-yes --purge remove ksks-sqoop";

    private UILogger logger;
    private Action agentInitAction;

    public ChainManager(UILogger logger) {
        this.logger = logger;
        agentInitAction = new AgentInitAction(logger);
    }

    public static void run(Chain chain) {
        chain.start(new Context());
    }

    public Chain getStatusChain() {

        CommandAction installStatusAction = new CommandAction(STATUS_COMMAND, new InstallStatusListener(logger));
        CommandAction serviceStatusAction = new CommandAction(SERVICE_COMMAND, new ServiceStatusListener(logger));

        return new Chain(agentInitAction, installStatusAction, serviceStatusAction);
    }

/*
    public Chain getChain(String command, String message, Action validationAction) {

        CommandAction exportAction = new CommandAction(command, new BasicListener(logger, message), true);

        return new Chain(agentInitAction, validationAction, exportAction);
    }

    public Chain getInstallChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new InstallStatusListener(logger));
        CommandAction installAction = new CommandAction(INSTALL_COMMAND, new InstallListener(logger));

        return new Chain(agentInitAction, statusAction, installAction);
    }

    public Chain getRemoveChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new RemoveStatusListener(logger));
        CommandAction removeAction = new CommandAction(REMOVE_COMMAND, new RemoveListener(logger));

        return new Chain(agentInitAction, statusAction, removeAction);
    }


*/
}
