package org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.slf4j.*;

public class StatusChainBuilder extends AbstractChainBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(StatusChainBuilder.class);

    private static final String DERBY_SERVICE_COMMAND = "service derby status";
    private static final String HIVE_SERVICE_COMMAND = "service hive-thrift status";

    public StatusChainBuilder(UILogger logger) {
        super(logger);
    }

    public Chain getChain() {
        return new Chain(agentInitAction,
                new CommandAction(STATUS_COMMAND, getInstallStatusListener() ),
                new CommandAction(DERBY_SERVICE_COMMAND, getDerbyServiceStatusListener() ),
                new CommandAction(HIVE_SERVICE_COMMAND, getHiveServiceStatusListener() )
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

                logger.info("Hive installed - OK");

                String msg = stdOut.contains("ksks-derby") ? "Derby installed - OK" : "Derby NOT INSTALLED";
                logger.info(msg);

                return true;
            }
        };
    }

    public ActionListener getDerbyServiceStatusListener() {
        return new BasicListener(logger, "Checking Derby service status...") {
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

    public ActionListener getHiveServiceStatusListener() {
        return new BasicListener(logger, "Checking Hive service status...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

                LOG.info("response: {}", response);
                LOG.info("stdOut: {}", stdOut);
                LOG.info("stdErr: {}", stdErr);

                logger.info(stdOut);
                logger.info(stdErr);
                logger.info("Completed");

                return false;
            }
        };
    }

}
