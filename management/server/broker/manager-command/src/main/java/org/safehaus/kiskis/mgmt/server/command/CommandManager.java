package org.safehaus.kiskis.mgmt.server.command;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceCommandInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:16 PM
 */
public class CommandManager implements CommandManagerInterface {

    private PersistenceCommandInterface persistenceCommand;
    private CommandTransportInterface commandTransport;
    private ArrayList<CommandListener> listeners = new ArrayList<CommandListener>();

    @Override
    public List<Command> getCommandList(Agent agent) {
        System.out.println(this.getClass().getName() + " getCommandList called");
        System.out.println(agent.toString());
        return null;
    }

    @Override
    public synchronized boolean executeCommand(Command command) {
        System.out.println(this.getClass().getName() + " saveCommand called");
        if (persistenceCommand.saveCommand(command)) {
            commandTransport.sendCommand(command);

            return true;
        }

        return false;
    }

    @Override
    public synchronized void registerCommand(Response response) {
        if(persistenceCommand.saveResponse(response)) {
            notifyListeners(response);
        }
    }

    private void notifyListeners(Response response) {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(listeners.size());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        try {
            for (CommandListener ai : listeners) {
                if (ai != null) {
                    if (ai.getName().equals(response.getSource())) {
                        ai.outputCommand(response);
                    }
                } else {
                    listeners.remove(ai);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setPersistenceCommandService(PersistenceCommandInterface persistenceCommand) {
        this.persistenceCommand = persistenceCommand;
        System.out.println(this.getClass().getName() + " PersistenceCommandInterface initialized");
    }

    public void setCommunicationService(CommandTransportInterface commandTransport) {
        this.commandTransport = commandTransport;
        System.out.println(this.getClass().getName() + " CommandTransportInterface initialized");
    }

    @Override
    public synchronized void addListener(CommandListener listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized void removeListener(CommandListener listener) {
        listeners.remove(listener);
    }

}
