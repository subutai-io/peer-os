package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.install.InstallStatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.install.InstallListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.remove.RemoveListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.remove.RemoveStatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.status.StatusListener;

public class ChainManager {

    private static final String STATUS_COMMAND = "dpkg -l|grep ksks";
    private static final String INSTALL_COMMAND = "apt-get --force-yes --assume-yes install ksks-pig";
    private static final String REMOVE_COMMAND = "apt-get --force-yes --assume-yes --purge remove ksks-pig";

    private static final Action INIT_ACTION = new InitAction();

    public static final Chain STATUS_CHAIN = getStatusChain();
    public static final Chain INSTALL_CHAIN = getInstallChain();
    public static final Chain REMOVE_CHAIN = getRemoveChain();

    private static Chain getStatusChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new StatusListener());

        return new Chain(INIT_ACTION, statusAction);
    }

    private static Chain getInstallChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new InstallStatusListener());
        CommandAction installAction = new CommandAction(INSTALL_COMMAND, new InstallListener());

        return new Chain(INIT_ACTION, statusAction, installAction);
    }

    private static Chain getRemoveChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new RemoveStatusListener());
        CommandAction removeAction = new CommandAction(REMOVE_COMMAND, new RemoveListener());

        return new Chain(INIT_ACTION, statusAction, removeAction);
    }

    public static void run(Chain chain) {
        chain.start(new Context());
    }
}
