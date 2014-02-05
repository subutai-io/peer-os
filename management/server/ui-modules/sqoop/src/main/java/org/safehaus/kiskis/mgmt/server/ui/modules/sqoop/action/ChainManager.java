package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.install.InstallListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.install.InstallStatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.remove.RemoveListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.remove.RemoveStatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.status.StatusListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;

public class ChainManager {

    private static final String STATUS_COMMAND = "dpkg -l|grep ksks";
    private static final String INSTALL_COMMAND = "apt-get --force-yes --assume-yes install ksks-sqoop";
    private static final String REMOVE_COMMAND = "apt-get --force-yes --assume-yes --purge remove ksks-sqoop";

    private static final String EXPORT_COMMAND = ". /etc/profile && sqoop export --connect ${connectString} --table ${table} --username ${username} --password ${password} --export-dir ${hdfsPath}";

    private static final String IMPORT_HDFS_COMMAND = ". /etc/profile && ${importUtil} --connect ${connectString} --username ${username} --password ${password} ${tableOption}";
    private static final String IMPORT_HIVE_COMMAND = ". /etc/profile && ${importUtil} --connect ${connectString} --username ${username} --password ${password} --hive-import ${tableOption}";
    private static final String IMPORT_HBASE_COMMAND = ". /etc/profile && sqoop import --connect ${connectString} --username ${username} --password ${password} --table ${table} --hbase-create-table --hbase-table ${hbaseTable} --column-family ${hbaseColumn}";

    protected UILogger logger;
    protected Action agentInitAction;

    public ChainManager(UILogger logger) {
        this.logger = logger;
        agentInitAction = new AgentInitAction(logger);
    }

    public static void run(Chain chain) {
        chain.start(new Context());
    }

    public Chain getStatusChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new StatusListener(logger));

        return new Chain(agentInitAction, statusAction);
    }

    public Chain getInstallChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new InstallStatusListener(logger));
        CommandAction installAction = new CommandAction(INSTALL_COMMAND, new InstallListener(logger));

        return new Chain(agentInitAction, statusAction, installAction);
    }

    public Chain getRemoveChain() {

        CommandAction statusAction = new CommandAction(STATUS_COMMAND, new RemoveStatusListener(logger));
        CommandAction removeAction = new CommandAction(REMOVE_COMMAND, new RemoveListener(logger));

        return new Chain(agentInitAction, statusAction, removeAction);
    }

    public Chain getExportChain(Action validationAction) {

        CommandAction exportAction = new CommandAction(EXPORT_COMMAND, new BasicListener(logger, "Export started, please wait..."), true);

        return new Chain(agentInitAction, validationAction, exportAction);
    }

    public Chain getHdfsImportChain(Action validationAction) {

        CommandAction importAction = new CommandAction(IMPORT_HDFS_COMMAND, new BasicListener(logger, "Import started, please wait..."), true);

        return new Chain(agentInitAction, validationAction, importAction);
    }

    public Chain getHiveImportChain(Action validationAction) {

        CommandAction importAction = new CommandAction(IMPORT_HIVE_COMMAND, new BasicListener(logger, "Import started, please wait..."), true);

        return new Chain(agentInitAction, validationAction, importAction);
    }

    public Chain getHBaseImportChain(Action validationAction) {

        CommandAction importAction = new CommandAction(IMPORT_HBASE_COMMAND, new BasicListener(logger, "Import started, please wait..."), true);

        return new Chain(agentInitAction, validationAction, importAction);
    }

}
