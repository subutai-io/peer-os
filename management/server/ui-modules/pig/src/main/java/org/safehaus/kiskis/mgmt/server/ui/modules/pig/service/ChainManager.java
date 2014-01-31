package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.install.InstallStatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.install.InstallListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.remove.RemoveListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.remove.RemoveStatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.status.StatusListener;

public class ChainManager {

    private static final String STATUS_COMMAND = "dpkg -l|grep ksks";
    private static final String INSTALL_COMMAND = "apt-get --force-yes --assume-yes install ksks-pig";
    private static final String REMOVE_COMMAND = "apt-get --force-yes --assume-yes --purge remove ksks-pig";

    public final Chain STATUS_CHAIN;
    public final Chain INSTALL_CHAIN;
    public final Chain REMOVE_CHAIN;

    public ChainManager(UILogger logger) {

        InitAction initAction = new InitAction(logger);

        STATUS_CHAIN = getStatusChain(logger, initAction);
        INSTALL_CHAIN = getInstallChain(logger, initAction);
        REMOVE_CHAIN = getRemoveChain(logger, initAction);
    }

    private static Chain getStatusChain(UILogger logger, Action initAction) {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new StatusListener(logger));

        return new Chain(initAction, statusAction);
    }

    private static Chain getInstallChain(UILogger logger, Action initAction) {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new InstallStatusListener(logger));
        CommandAction installAction = new CommandAction(INSTALL_COMMAND, new InstallListener(logger));

        return new Chain(initAction, statusAction, installAction);
    }

    private static Chain getRemoveChain(UILogger logger, Action initAction) {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new RemoveStatusListener(logger));
        CommandAction removeAction = new CommandAction(REMOVE_COMMAND, new RemoveListener(logger));

        return new Chain(initAction, statusAction, removeAction);
    }

    public static void run(Chain chain) {
        chain.start(new Context());
    }
}
