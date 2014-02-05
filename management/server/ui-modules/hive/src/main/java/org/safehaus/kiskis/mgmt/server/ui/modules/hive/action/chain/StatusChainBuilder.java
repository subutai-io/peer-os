package org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class StatusChainBuilder extends AbstractChainBuilder {

    private static final String SERVICE_COMMAND = "service hive-thrift status";

    public StatusChainBuilder(UILogger logger) {
        super(logger);
    }

    public Chain getChain() {
        return new Chain(agentInitAction,
                new CommandAction(STATUS_COMMAND, getInstallStatusListener()),
                new CommandAction(SERVICE_COMMAND, getServiceStatusListener())
        );
    }

    public ActionListener getInstallStatusListener() {
        return new BasicListener(logger, "Checking installation status, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                if (!handleHadoopStatus(stdOut)) {
                    return false;
                } else if (!stdOut.contains("ksks-hive")) {
                    logger.complete("Hive NOT INSTALLED");
                    return false;
                }

                String msg = stdOut.contains("ksks-derby") ? "Derby installed - OK" : "Derby NOT INSTALLED";
                logger.info(msg);

                return true;
            }
        };
    }

    public ActionListener getServiceStatusListener() {
        return new BasicListener(logger, "Checking service status, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                logger.complete(stdOut);
                return false;
            }
        };
    }

}
