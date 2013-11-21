package org.safehaus.kiskis.mgmt.server.command;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:16 PM
 */
public class CommandManager implements CommandManagerInterface, BrokerListener {

    private BundleContext context;
    private PersistenceInterface persistenceCommand;
    private ArrayList<CommandListener> listeners = new ArrayList<CommandListener>();

//    @Override
//    public List<Command> getCommandList(Agent agent) {
//        System.out.println(this.getClass().getName() + " getCommandList called");
//        System.out.println(agent.toString());
//        return null;
//    }

    @Override
    public void executeCommand(Command command) {
        try {
            if (persistenceCommand.saveCommand(command)) {
                getCommandTransport().sendCommand(command);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized void getCommand(Response response) {
        switch (response.getType()) {
            case EXECUTE_RESPONSE: {
                persistenceCommand.saveResponse(response);
                break;
            }
            case EXECUTE_RESPONSE_DONE: {
                persistenceCommand.saveResponse(response);
                break;
            }
            default: {
                break;
            }
        }
        notifyListeners(response);
    }

    private void notifyListeners(Response response) {
        try {
            System.out.println("Количество модулей:" + listeners.size());
            for (CommandListener ai : (ArrayList<CommandListener>) listeners.clone()) {
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

    @Override
    public synchronized void addListener(CommandListener listener) {
        try {
            System.out.println("Adding module listener : " + listener.getName());
            listeners.add(listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized void removeListener(CommandListener listener) {
        try {
            System.out.println("Removing module listener : " + listener.getName());
            listeners.remove(listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void init() {
        try {
            getCommandTransport().addListener(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void destroy() {
        try {
            getCommandTransport().removeListener(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setPersistenceCommandService(PersistenceInterface persistenceCommand) {
        this.persistenceCommand = persistenceCommand;
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    private CommandTransportInterface getCommandTransport() {
        ServiceReference reference = context
                .getServiceReference(CommandTransportInterface.class.getName());
        return (CommandTransportInterface) context.getService(reference);
    }
}
