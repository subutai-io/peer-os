package org.safehaus.kiskis.mgmt.impl.sqoop;

import org.safehaus.kiskis.mgmt.api.sqoop.Config;
import org.safehaus.kiskis.mgmt.api.sqoop.setting.CommonSetting;
import org.safehaus.kiskis.mgmt.api.sqoop.setting.ExportSetting;
import org.safehaus.kiskis.mgmt.api.sqoop.setting.ImportSetting;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

class Requests {

    private static final String EXEC_PROFILE = ". /etc/profile";
    public static final String PACKAGE_NAME = "ksks-sqoop";

    enum Type {

        INSTALL,
        PURGE,
        IMPORT,
        EXPORT;

    }

    static Request make(Type type) {
        return make(type, 0, null);
    }

    static Request make(Type type, int timeout) {
        return make(type, timeout, null);
    }

    static Request make(Type type, int timeout, CommonSetting settings) {
        Request req = getRequestTemplate();
        StringBuilder sb;
        switch(type) {
            case INSTALL:
            case PURGE:
                sb = new StringBuilder("apt-get --force-yes --assume-yes ");
                sb.append(type.toString().toLowerCase()).append(" ");
                sb.append(PACKAGE_NAME);
                req.setProgram(sb.toString());
                break;
            case IMPORT:
                if(settings instanceof ImportSetting)
                    req = importData((ImportSetting)settings, timeout);
                break;
            case EXPORT:
                if(settings instanceof ExportSetting)
                    req = exportData((ExportSetting)settings, timeout);
                break;
            default:
                throw new AssertionError(type.name());
        }
        if(timeout > 0) req.setTimeout(timeout);
        return req;
    }

    static Request exportData(ExportSetting settings, int timeout) {
        StringBuilder sb = new StringBuilder();
        sb.append(EXEC_PROFILE).append(" && ");
        sb.append("sqoop export");
        appendOption(sb, "connect", settings.getConnectionString());
        appendOption(sb, "username", settings.getUsername());
        appendOption(sb, "password", settings.getPassword());
        appendOption(sb, "table", settings.getTableName());
        appendOption(sb, "export-dir", settings.getHdfsPath());

        Request req = getRequestTemplate();
        req.setProgram(sb.toString());
        if(timeout > 0) req.setTimeout(timeout);
        return req;
    }

    static Request importData(ImportSetting settings, int timeout) {
        boolean all = settings.getBooleanParameter("import-all-tables");
        StringBuilder sb = new StringBuilder();
        sb.append(EXEC_PROFILE).append(" && ");
        switch(settings.getType()) {
            case HDFS:
                if(all) sb.append("sqoop-import-all-tables");
                else sb.append("sqoop-import");
                appendOption(sb, "connect", settings.getConnectionString());
                appendOption(sb, "username", settings.getUsername());
                appendOption(sb, "password", settings.getPassword());
                if(!all) appendOption(sb, "table", settings.getTableName());
                break;
            case HBASE:
                sb.append("sqoop import");
                appendOption(sb, "connect", settings.getConnectionString());
                appendOption(sb, "username", settings.getUsername());
                appendOption(sb, "password", settings.getPassword());
                appendOption(sb, "table", settings.getTableName());
                appendOption(sb, "hbase-create-table", null);
                appendOption(sb, "hbase-table", settings.getStringParameter("hbase-table"));
                appendOption(sb, "column-family", settings.getStringParameter("column-family"));
                break;
            case HIVE:
                if(all) sb.append("sqoop-import-all-tables");
                else sb.append("sqoop-import");
                appendOption(sb, "connect", settings.getConnectionString());
                appendOption(sb, "username", settings.getUsername());
                appendOption(sb, "password", settings.getPassword());
                appendOption(sb, "hive-import", null);
                if(!all) {
                    appendOption(sb, "table", settings.getTableName());
                    String db = settings.getStringParameter("hive-database");
                    String tb = settings.getStringParameter("hive-table");
                    appendOption(sb, "hive-table", db + "." + tb);
                }
                break;
            default:
                throw new AssertionError(settings.getType().name());
        }

        Request req = getRequestTemplate();
        req.setProgram(sb.toString());
        if(timeout > 0) req.setTimeout(timeout);
        return req;
    }

    static Request packageList() {
        Request req = getRequestTemplate();
        req.setProgram("dpkg -l | grep '^ii' | grep ksks");
        return req;
    }

    static Request getRequestTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                Config.PRODUCT_KEY, //     source
                null, //                        !! task uuid
                1, //                           !! request sequence number
                "/", //                         cwd
                "pwd", //                        program
                OutputRedirection.RETURN, //    std output redirection
                OutputRedirection.RETURN, //    std error redirection
                null, //                        stdout capture file path
                null, //                        stderr capture file path
                "root", //                      runas
                null, //                        arg
                null, //                        env vars
                30); //
    }

    private static void appendOption(StringBuilder sb, String option, String value) {
        sb.append(" --").append(option);
        if(value != null) sb.append(" ").append(value);
    }
}
