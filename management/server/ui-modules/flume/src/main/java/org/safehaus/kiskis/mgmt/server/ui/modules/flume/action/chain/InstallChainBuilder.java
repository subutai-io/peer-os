package org.safehaus.kiskis.mgmt.server.ui.modules.flume.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.flume.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallChainBuilder extends AbstractChainBuilder {

    private static final String INSTALL_COMMAND = "apt-get update && apt-get --force-yes --assume-yes install ksks-flume";

    public InstallChainBuilder(UILogger logger) {
        super(logger);
    }

    public Chain getChain() {
        return new Chain(agentInitAction,
                new CommandAction(STATUS_COMMAND, getStatusListener()),
                new CommandAction(INSTALL_COMMAND, getInstallListener())
        );
    }

    public ActionListener getStatusListener() {
        return new BasicListener(logger, "Checking status before installing, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                if (!handleHadoopStatus(stdOut)) {
                    return false;
                } else if (stdOut.contains("ksks-flume")) {
                    logger.complete("Flume ALREADY INSTALLED.");
                    return false;
                }

                return true;
            }
        };
    }

    public ActionListener getInstallListener() {
        return new BasicListener(logger, "Installing Flume, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

                if (response.getExitCode() != null && response.getExitCode() != 0) {
                    logger.complete("Error occurred while installing Flume. Please see the server logs for details.");
                    return false;
                }

                logger.info("Flume installed successfully");

                return true;
            }
        };
    }
}
