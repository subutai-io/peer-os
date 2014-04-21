package org.safehaus.kiskis.mgmt.impl.hive;

import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

class Requests {

    private static final String EXEC_PROFILE = ". /etc/profile";

    enum Type {

        INSTALL,
        PURGE,
        STATUS,
        START,
        STOP,
        RESTART;
    }

    static Request make(Type type, Product product) {
        return make(type, product, 0);
    }

    static Request make(Type type, Product product, int timeout) {
        Request req = getRequestTemplate();
        StringBuilder sb;
        switch(type) {
            case INSTALL:
            case PURGE:
                sb = new StringBuilder("apt-get --force-yes --assume-yes ");
                sb.append(type.toString().toLowerCase()).append(" ");
                sb.append(product.getPackageName());
                req.setProgram(sb.toString());
                break;
            case STATUS:
            case START:
            case STOP:
            case RESTART:
                sb = new StringBuilder();
                if(product.isProfileScriptRun())
                    sb.append(EXEC_PROFILE).append(" && ");
                sb.append("service ").append(product.getServiceName());
                sb.append(" ").append(type.toString().toLowerCase());
                req.setProgram(sb.toString());
                break;
            default:
                throw new AssertionError(type.name());
        }
        if(timeout > 0) req.setTimeout(timeout);
        return req;
    }

    static Request configureHiveServer(String host) {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && hive-configure.sh " + host); // provide IP address of server
        return req;
    }

    static Request addHivePoperty(String cmd, String propFile, String property, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append(". /etc/profile && hive-property.sh ").append(cmd).append(" ");
        sb.append(propFile).append(" ").append(property);
        if(value != null) sb.append(" ").append(value);

        Request req = getRequestTemplate();
        req.setProgram(sb.toString());
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

}
