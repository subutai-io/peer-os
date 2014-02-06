package org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class ServiceChainBuilder extends AbstractChainBuilder {

    private static final String SERVICE_START_COMMAND = "service hive-thrift start";
    private static final String SERVICE_STOP_COMMAND = "service hive-thrift stop";

    private String command;
    private String message;

    private ServiceChainBuilder(UILogger logger, String command, String message) {
        super(logger);
        this.command = command;
        this.message = message;
    }

    public Chain getChain() {
        return new Chain(agentInitAction,
            new CommandAction(command, getActionListener())
        );
    }

    private ActionListener getActionListener() {
        return new BasicListener(logger, message) {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                logger.info(stdOut);
                logger.info(stdErr);
                logger.info("Completed");
                return false;
            }
        };
    }

    public static Chain getStartChain(UILogger logger) {
        return new ServiceChainBuilder(logger, SERVICE_START_COMMAND, "Starting the service, please wait...").getChain();
    }

    public static Chain getStopChain(UILogger logger) {
        return new ServiceChainBuilder(logger, SERVICE_STOP_COMMAND, "Stopping the service, please wait...").getChain();
    }

}
