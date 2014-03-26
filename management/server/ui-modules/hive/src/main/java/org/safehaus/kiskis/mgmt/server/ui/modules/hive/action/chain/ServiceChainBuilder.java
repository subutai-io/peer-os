package org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceChainBuilder extends AbstractChainBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceChainBuilder.class);

    private static final String DERBY_START = "service derby start";
    private static final String HIVE_START = ". /etc/profile && service hive-thrift start";

    private static final String DERBY_STOP = "service derby stop";
    private static final String HIVE_STOP = ". /etc/profile && service hive-thrift stop";

    private String commands[];
    private String message;

    private ServiceChainBuilder(UILogger logger, String message, String ... commands) {
        super(logger);
        this.message = message;
        this.commands = commands;
    }

    public Chain getChain() {

        Chain chain = new Chain(agentInitAction);

        for (String command : commands) {
            chain.add( new CommandAction( command, getActionListener() ) );
        }

        return chain;
    }

    private ActionListener getActionListener() {

        return new BasicListener(logger, message) {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

                LOG.info("response: {}", response);
                LOG.info("stdOut: {}", stdOut);
                LOG.info("stdErr: {}", stdErr);

                logger.info(stdOut);
                logger.info(stdErr);

                return true;
            }
        };
    }

    public static Chain getStartChain(UILogger logger) {
        return new ServiceChainBuilder(logger, "Starting services...",
                DERBY_START,
                HIVE_START
        ).getChain();
    }

    public static Chain getStopChain(UILogger logger) {
        return new ServiceChainBuilder(logger, "Stopping services...",
                HIVE_STOP,
                DERBY_STOP
        ).getChain();
    }

}
