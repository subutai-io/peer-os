package org.safehaus.kiskis.mgmt.server.ui.modules.lucene.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class StatusChainBuilder extends AbstractChainBuilder {

    public StatusChainBuilder(UILogger logger) {
        super(logger);
    }

    public Chain getChain() {
        return new Chain(agentInitAction,
                new CommandAction(STATUS_COMMAND, getInstallStatusListener())
        );
    }

    public ActionListener getInstallStatusListener() {
        return new BasicListener(logger, "Checking status, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

                String msg = stdOut.contains("ksks-lucene") ? "Lucene installed - OK" : "Lucene NOT INSTALLED";
                logger.complete(msg);

                return false;
            }
        };
    }
}
