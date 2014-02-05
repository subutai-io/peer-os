package org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.command;

import org.safehaus.kiskis.mgmt.server.ui.modules.flume.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

public class CommandBuilder {

    private static String source;

    public static void setSource(String source) {
        CommandBuilder.source = source;
    }

    public static Command getCommand(Context context, String commandLine) {

        Agent agent = context.get("agent");

        return CommandFactory.createRequest(
                RequestType.EXECUTE_REQUEST, // type
                agent.getUuid(), // agent uuid
                source, // source
                null, // task uuid
                1, // request sequence number
                "/", // cwd
                commandLine, // program
                OutputRedirection.RETURN, // std output redirection
                OutputRedirection.RETURN, // std error redirection
                null, // stdout capture file path
                null, // stderr capture file path
                "root", // runas
                null, // arg
                null, // env vars
                30  // timeout
        );
    }
}
