package org.safehaus.kiskis.mgmt.server.ui.modules.flume.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.flume.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.flume.view.UILogger;
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
        return new BasicListener(logger, "Checking installation status, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                if (!handleHadoopStatus(stdOut)) {
                    return false;
                }

                String msg = stdOut.contains("ksks-flume") ? "Flume installed - OK" : "Flume NOT INSTALLED";
                logger.complete(msg);

                return false;
            }
        };
    }
}
