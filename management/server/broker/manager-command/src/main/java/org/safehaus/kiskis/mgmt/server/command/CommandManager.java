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
    public synchronized void executeCommand(Command command) {
        try{
            if (persistenceCommand.saveCommand(command)) {
                commandTransport.sendCommand(command);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized void registerCommand(Response response) {
        System.out.println("~~~~~~~ Command registered");
        System.out.println(response);
        System.out.println();
        try{
            if(persistenceCommand.saveResponse(response)) {
                notifyListeners(response);
            }
        }  catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void notifyListeners(Response response) {
        try {
            for (CommandListener ai : listeners) {
                if (ai != null && ai.getName() != null) {
                    if (ai.getName().equals(response.getSource())) {
                        System.out.println("~~~~~~~ Listeners notified");
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
    }

    public void setCommunicationService(CommandTransportInterface commandTransport) {
        this.commandTransport = commandTransport;
    }

    @Override
    public synchronized void addListener(CommandListener listener) {
        try{
            System.out.println("Adding module listener : " + listener.getName());
            listeners.add(listener);
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized void removeListener(CommandListener listener) {
        try{
            listeners.remove(listener);
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
