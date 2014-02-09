package org.safehaus.kiskis.mgmt.server.ui.modules.lucene.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class RemoveChainBuilder extends AbstractChainBuilder {

    private static final String REMOVE_COMMAND = "apt-get --force-yes --assume-yes --purge remove ksks-lucene";

    public RemoveChainBuilder(UILogger logger) {
        super(logger);
    }

    public Chain getChain() {
        return new Chain(agentInitAction,
                new CommandAction(STATUS_COMMAND, getStatusListener()),
                new CommandAction(REMOVE_COMMAND, getRemoveListener())
        );
    }

    public ActionListener getStatusListener() {
        return new BasicListener(logger, "Checking status, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                if (stdOut == null || !stdOut.contains("ksks-lucene")) {
                    logger.complete("Lucene NOT INSTALLED. Nothing to remove.");
                    return false;
                }
                return true;
            }
        };
    }

    public ActionListener getRemoveListener() {
        return new BasicListener(logger, "Removing Lucene, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                String msg = response.getExitCode() == null || response.getExitCode() == 0
                        ? "Lucene removed successfully"
                        : "Error occurred while removing. Please see the server logs for details.";

                logger.complete(msg);
                return false;
            }
        };
    }
}
