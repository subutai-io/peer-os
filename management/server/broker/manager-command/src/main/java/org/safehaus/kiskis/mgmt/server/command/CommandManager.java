package org.safehaus.kiskis.mgmt.server.command;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceCommandInterface;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:16 PM
 */
public class CommandManager implements CommandManagerInterface {

    private PersistenceCommandInterface persistenceCommand;
    private CommandTransportInterface commandTransport;

    @Override
    public List<Command> getCommandList(Agent agent) {
        System.out.println(this.getClass().getName() + " getCommandList called");
        System.out.println(agent.toString());
        return null;
    }

    @Override
    public boolean executeCommand(Command command) {
        System.out.println(this.getClass().getName() + " saveCommand called");
        persistenceCommand.saveCommand(command);
        commandTransport.sendCommand(command);
        return false;
    }

    public void setPersistenceCommandService(PersistenceCommandInterface persistenceCommand) {
        this.persistenceCommand = persistenceCommand;
        System.out.println(this.getClass().getName() + " PersistenceCommandInterface initialized");
    }

    public void setCommunicationService(CommandTransportInterface commandTransport) {
        this.commandTransport = commandTransport;
        System.out.println(this.getClass().getName() + " CommandTransportInterface initialized");
    }

}
