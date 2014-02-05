package org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.chain;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.BasicListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class RemoveChainBuilder extends AbstractChainBuilder {

    private static final String HIVE_REMOVE_COMMAND = "apt-get --force-yes --assume-yes --purge remove ksks-hive";
    private static final String DERBY_REMOVE_COMMAND = "apt-get --force-yes --assume-yes --purge remove ksks-derby";

    private static final String HIVE_NOT_INSTALLED = "hiveNotInstalled";

    public RemoveChainBuilder(UILogger logger) {
        super(logger);
    }

    public Chain getChain() {
        return new Chain(agentInitAction,
                new CommandAction(STATUS_COMMAND, getStatusListener()),
                new CommandAction(HIVE_REMOVE_COMMAND, getHiveRemoveListener()),
                new CommandAction(DERBY_REMOVE_COMMAND, getDerbyRemoveListener())
        );
    }

    public ActionListener getStatusListener() {
        return new BasicListener(logger, "Checking status before removing, please wait...") {
            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                context.put(HIVE_NOT_INSTALLED, !stdOut.contains("ksks-hive"));
                return true;
            }
        };
    }

    public ActionListener getHiveRemoveListener() {
        return new BasicListener(logger, "Removing Hive, please wait...") {

            @Override
            protected Result onStart(Context context, String programLine) {

                String msg = executeMessage;
                Result result = Result.CONTINUE;

                if (context.get(HIVE_NOT_INSTALLED)) {
                    msg = "Hive NOT INSTALLED. Nothing to remove.";
                    result = Result.SKIP;
                }

                logger.info(msg);
                return result;
            }

            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                String msg = response.getExitCode() == null || response.getExitCode() == 0
                        ? "Hive removed successfully"
                        : "Error occurred while removing Hive. Please see the server logs for details.";

                logger.complete(msg);
                return false;
            }
        };
    }

    public ActionListener getDerbyRemoveListener() {
        return new BasicListener(logger, "Removing Derby, please wait...") {

            @Override
            protected Result onStart(Context context, String programLine) {
                if (context.get(HIVE_NOT_INSTALLED)) {
                    logger.complete("Derby NOT INSTALLED. Nothing to remove.");
                    return Result.INTERRUPT;
                }

                logger.info(executeMessage);
                return Result.CONTINUE;
            }

            @Override
            protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
                String msg = response.getExitCode() == null || response.getExitCode() == 0
                        ? "Derby removed successfully"
                        : "Error occurred while removing Derby. Please see the server logs for details.";

                logger.complete(msg);
                return false;
            }
        };
    }

}
