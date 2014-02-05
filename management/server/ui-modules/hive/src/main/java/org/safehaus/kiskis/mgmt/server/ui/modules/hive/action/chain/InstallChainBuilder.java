package org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.AgentInitAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallChainBuilder {

    private static final String STATUS_COMMAND = "dpkg -l|grep ksks";
    private static final String SERVICE_COMMAND = "service hive-thrift status";
    private static final String INSTALL_COMMAND = "apt-get --force-yes --assume-yes install ksks-hive";
//    private static final String REMOVE_COMMAND = "apt-get --force-yes --assume-yes --purge remove ksks-sqoop";

    private UILogger logger;
    private Action agentInitAction;

    public InstallChainBuilder(UILogger logger) {
        this.logger = logger;
        agentInitAction = new AgentInitAction(logger);
    }


    public Chain getStatusChain() {

        ActionListener installStatusListener =  new BasicListener(logger, "Checking installation status, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                if (stdOut == null || !stdOut.contains("ksks-hadoop")) {
                    logger.complete("Hadoop NOT INSTALLED. Please install hadoop before installing Hive.");
                    return false;
                }

                logger.info("Hadoop installed - OK");

                if (!stdOut.contains("ksks-hive")) {
                    logger.complete("Hive NOT INSTALLED");
                    return false;
                }

                String msg = stdOut.contains("ksks-derby") ? "Derby installed - OK" : "Derby NOT INSTALLED";
                logger.info(msg);

                return true;
            }
        };

        ActionListener serviceStatusListener =  new BasicListener(logger, "Checking service status, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                logger.complete(stdOut);
                return false;
            }
        };

        CommandAction installStatusAction = new CommandAction(STATUS_COMMAND, installStatusListener);
        CommandAction serviceStatusAction = new CommandAction(SERVICE_COMMAND, serviceStatusListener);

        return new Chain(agentInitAction, installStatusAction, serviceStatusAction);
    }

    public Chain getInstallChain() {

        ActionListener beforeListener =  new BasicListener(logger, "Checking status before installing, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                if (stdOut == null || !stdOut.contains("ksks-hadoop")) {
                    logger.complete("Hadoop NOT INSTALLED. Please install Hadoop before installing Sqoop.");
                    return false;
                }

                logger.info("Hadoop installed - OK");

                if (stdOut.contains("ksks-hive")) {
                    logger.complete("Hive ALREADY INSTALLED");
                    return false;
                }
                return true;
            }
        };

        ActionListener installListener =  new BasicListener(logger, "Installing Hive, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                String msg = response.getExitCode() == null || response.getExitCode() == 0
                        ? "Hive installed successfully"
                        : "Error occurred while installing Hive. Please see the server logs for details.";

                logger.complete(msg);
                return false;
            }
        };

        CommandAction beforeAction = new CommandAction(STATUS_COMMAND, beforeListener);
        CommandAction installAction = new CommandAction(INSTALL_COMMAND, installListener);

        return new Chain(agentInitAction, beforeAction, installAction);
    }


}
