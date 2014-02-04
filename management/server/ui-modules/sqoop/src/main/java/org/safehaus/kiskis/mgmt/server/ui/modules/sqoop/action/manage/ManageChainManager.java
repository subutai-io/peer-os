package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.InitAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.install.InstallStatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.install.InstallListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.remove.RemoveListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.remove.RemoveStatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.status.StatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;

public class ManageChainManager extends ChainManager {

    private static final String STATUS_COMMAND = "dpkg -l|grep ksks";
    private static final String INSTALL_COMMAND = "apt-get --force-yes --assume-yes install ksks-sqoop";
    private static final String REMOVE_COMMAND = "apt-get --force-yes --assume-yes --purge remove ksks-sqoop";

    public ManageChainManager(UILogger logger) {
        super(logger);
    }

    public Chain getStatusChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new StatusListener(logger));

        return new Chain(initAction, statusAction);
    }

    public Chain getInstallChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new InstallStatusListener(logger));
        CommandAction installAction = new CommandAction(INSTALL_COMMAND, new InstallListener(logger));

        return new Chain(initAction, statusAction, installAction);
    }

    public Chain getRemoveChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new RemoveStatusListener(logger));
        CommandAction removeAction = new CommandAction(REMOVE_COMMAND, new RemoveListener(logger));

        return new Chain(initAction, statusAction, removeAction);
    }
}
