package org.safehaus.kiskis.mgmt.server.ui.modules.lucene.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallChainBuilder extends AbstractChainBuilder {

    private static final String INSTALL_COMMAND = "apt-get update && apt-get --force-yes --assume-yes install ksks-lucene";

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
        return new BasicListener(logger, "Checking status, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                if (stdOut.contains("ksks-lucene")) {
                    logger.complete("Lucene ALREADY INSTALLED.");
                    return false;
                }

                return true;
            }
        };
    }

    public ActionListener getInstallListener() {
        return new BasicListener(logger, "Installing Lucene, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

                String msg = response.getExitCode() == null || response.getExitCode() == 0
                    ? "Lucene installed successfully"
                    : "Error occurred while installing. Please see the server logs for details.";

                logger.complete(msg);
                return false;
            }
        };
    }
}
