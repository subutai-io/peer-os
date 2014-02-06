package org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class InstallChainBuilder extends AbstractChainBuilder {

    private static final String HIVE_INSTALL_COMMAND = "apt-get --force-yes --assume-yes install ksks-hive";
    private static final String DERBY_INSTALL_COMMAND = "apt-get --force-yes --assume-yes install ksks-derby";

    private static final String HIVE_ALREADY_INSTALLED = "hiveAlreadyInstalled";
    private static final String DERBY_ALREADY_INSTALLED = "derbyAlreadyInstalled";

    public InstallChainBuilder(UILogger logger) {
        super(logger);
    }

    public Chain getChain() {
        return new Chain(agentInitAction,
                new CommandAction(STATUS_COMMAND, getStatusListener()),
                new CommandAction(HIVE_INSTALL_COMMAND, getHiveInstallListener()),
                new CommandAction(DERBY_INSTALL_COMMAND, getDerbyInstallListener())
        );
    }

    public ActionListener getStatusListener() {
        return new BasicListener(logger, "Checking status before installing, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                if (!handleHadoopStatus(stdOut)) {
                    return false;
                }

                context.put(HIVE_ALREADY_INSTALLED, stdOut.contains("ksks-hive"));
                context.put(DERBY_ALREADY_INSTALLED, stdOut.contains("ksks-derby"));

                return true;
            }
        };
    }

    public ActionListener getHiveInstallListener() {
        return new BasicListener(logger, "Installing Hive, please wait...") {

            @Override
            protected Result onStart(Context context, String programLine) {

                String msg = executeMessage;
                Result result = Result.CONTINUE;

                if (context.get(HIVE_ALREADY_INSTALLED)) {
                    msg = "Hive ALREADY INSTALLED";
                    result = Result.SKIP;
                }

                logger.info(msg);
                return result;
            }

            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {

                if (response.getExitCode() != null && response.getExitCode() != 0) {
                    logger.complete("Error occurred while installing Hive. Please see the server logs for details.");
                    return false;
                }

                logger.info("Hive installed successfully");
                return true;
            }
        };
    }

    public ActionListener getDerbyInstallListener() {
        return new BasicListener(logger, "Installing Derby, please wait...") {

            @Override
            protected Result onStart(Context context, String programLine) {
                if (context.get(DERBY_ALREADY_INSTALLED)) {
                    logger.complete("Derby ALREADY INSTALLED");
                    return Result.INTERRUPT;
                }

                logger.info(executeMessage);
                return Result.CONTINUE;
            }

            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                String msg = response.getExitCode() == null || response.getExitCode() == 0
                        ? "Derby installed successfully"
                        : "Error occurred while installing Derby. Please see the server logs for details.";

                logger.complete(msg);
                return false;
            }
        };
    }

}
