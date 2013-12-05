package org.safehaus.kiskis.mgmt.server.ui.modules.terminal;

import com.google.common.base.Strings;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.*;

public class Terminal implements Module {

    public static final String MODULE_NAME = "Terminal";
    private BundleContext context;
    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private Task task;
        private final TextField textFieldWorkingDirectory;
        private final TextField textFieldProgram;
        private final TextField textFieldRunAs;
        private final TextField textFieldArgs;
        private final TextField textFieldTimeout;
        private final TextArea textAreaCommand;
        private final TextArea textAreaOutput;
        private Set<Agent> agents;
        private final CommandManagerInterface commandManagerInterface;

        public ModuleComponent(final CommandManagerInterface commandManagerInterface) {
            this.commandManagerInterface = commandManagerInterface;

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setMargin(true);

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setSpacing(true);
            horizontalLayout.setMargin(true);

            textFieldWorkingDirectory = new TextField("Working Directory");
            textFieldWorkingDirectory.setValue("/home");

            textFieldProgram = new TextField("Program");
            textFieldProgram.setWidth("250px");
            textFieldProgram.setValue("ls");

            textFieldRunAs = new TextField("Run As");
            textFieldRunAs.setValue("root");

            textFieldArgs = new TextField("Args");

            textFieldTimeout = new TextField("Timeout");
            textFieldTimeout.setValue("180");

            Label labelText = new Label("Enter command:");

            textAreaCommand = new TextArea();
            textAreaCommand.setRows(5);
            textAreaCommand.setColumns(80);
            textAreaCommand.setImmediate(true);
            textAreaCommand.setWordwrap(true);

            verticalLayout.addComponent(labelText);
            verticalLayout.addComponent(textAreaCommand);

            horizontalLayout.addComponent(textFieldWorkingDirectory);
            horizontalLayout.addComponent(textFieldProgram);
            horizontalLayout.addComponent(textFieldRunAs);
            horizontalLayout.addComponent(textFieldArgs);
            horizontalLayout.addComponent(textFieldTimeout);
            verticalLayout.addComponent(horizontalLayout);

            HorizontalLayout hLayout = new HorizontalLayout();
            Button buttonSend = genSendButton();
            Button getRequests = genGetRequestButton();
            Button getResponses = genGetResponsesButton();
            Button getTasks = getGetTasksButton();
            Button truncateTables = getTruncateTablesButton();
            Button buttonGetPhysicalAgents = getPhysicalAgents();
            Button buttonGetLxcAgents = getLxcAgents();
            Button buttonCreateCluster = getClusterButton();

            hLayout.addComponent(buttonSend);
            hLayout.addComponent(getRequests);
            hLayout.addComponent(getResponses);
            hLayout.addComponent(getTasks);
            hLayout.addComponent(truncateTables);
            hLayout.addComponent(buttonGetPhysicalAgents);
            hLayout.addComponent(buttonGetLxcAgents);
            hLayout.addComponent(buttonCreateCluster);

            verticalLayout.addComponent(hLayout);

            Label labelOutput = new Label("Commands output");
            textAreaOutput = new TextArea();
            textAreaOutput.setRows(20);
            textAreaOutput.setColumns(80);
            textAreaOutput.setImmediate(true);
            textAreaOutput.setWordwrap(false);

            verticalLayout.addComponent(labelOutput);
            verticalLayout.addComponent(textAreaOutput);

            setCompositionRoot(verticalLayout);
        }

        @Override
        public synchronized void outputCommand(Response response) {
            try {
                if (task != null && response != null && response.getSource().equals(MODULE_NAME)) {
                    StringBuilder sb = new StringBuilder();

                    if (response.getTaskUuid() != null
                            && response.getTaskUuid().compareTo(task.getUuid()) == 0) {

                        if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                            task.setTaskStatus(TaskStatus.SUCCESS);
                            commandManagerInterface.saveTask(task);
                        }

                        sb.append("\n");
                        Response result = commandManagerInterface.getResponse(response.getTaskUuid(),
                                response.getRequestSequenceNumber());
                        sb.append(CommandJson.getJson(new Command(result)));
                    }

                    textAreaOutput.setValue(sb);
                    textAreaOutput.setCursorPosition(sb.length() - 1);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        @Override
        public String getName() {
            return Terminal.MODULE_NAME;
        }

        private Button getPhysicalAgents() {
            Button button = new Button("Get physical agents");
            button.setDescription("Gets agents from Cassandra");
            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Set<Agent> agents = getAgentManager().getRegisteredPhysicalAgents();
                    StringBuilder sb = new StringBuilder();

                    for (Agent agent : agents) {
                        sb.append(agent).append("\n");

                        Set<Agent> childAgents = getAgentManager().getChildLxcAgents(agent);
                        for (Agent lxcAgent : childAgents) {
                            sb.append("\t").append(lxcAgent).append("\n");
                        }
                    }
                    textAreaOutput.setValue(sb.toString());
                }
            });
            return button;
        }

        private Button getLxcAgents() {
            Button button = new Button("Get LXC agents");
            button.setDescription("Gets LXC agents from Cassandra");
            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Set<Agent> agents = getAgentManager().getRegisteredLxcAgents();
                    StringBuilder sb = new StringBuilder();

                    for (Agent agent : agents) {
                        sb.append(agent).append("\n");
                    }
                    textAreaOutput.setValue(sb.toString());
                }
            });
            return button;
        }

        private Button genSendButton() {
            Button button = new Button("Send");
            button.setDescription("Sends command to agent");
            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    try {
                        agents = AppData.getSelectedAgentList();
                        if (agents != null && agents.size() > 0) {
                            for (Agent agent : agents) {
                                if (!Strings.isNullOrEmpty(textAreaCommand.getValue().toString())) {
                                    String json = textAreaCommand.getValue().toString().trim();

                                    Request r = CommandJson.getRequest(json);

                                    if (r != null) {
                                        task = new Task();
                                        task.setDescription("JSON executing");
                                        task.setTaskStatus(TaskStatus.NEW);
                                        commandManagerInterface.saveTask(task);

                                        r.setUuid(agent.getUuid());
                                        r.setSource(Terminal.MODULE_NAME);
                                        r.setTaskUuid(task.getUuid());

                                        Command command = new Command(r);
                                        commandManagerInterface.executeCommand(command);
                                    }else{
                                        textAreaOutput.setValue("ERROR IN COMMAND JSON");
                                    }
                                } else {
                                    task = new Task();
                                    task.setDescription("JSON executing");
                                    task.setTaskStatus(TaskStatus.NEW);
                                    commandManagerInterface.saveTask(task);

                                    Request r = new Request();

                                    r.setUuid(agent.getUuid());
                                    r.setSource(Terminal.MODULE_NAME);
                                    r.setTaskUuid(task.getUuid());
                                    r.setType(RequestType.EXECUTE_REQUEST);
                                    r.setRequestSequenceNumber(task.getIncrementedReqSeqNumber());
                                    r.setWorkingDirectory(textFieldWorkingDirectory.getValue().toString());
                                    r.setProgram(textFieldProgram.getValue().toString());
                                    r.setStdOut(OutputRedirection.CAPTURE_AND_RETURN);
                                    r.setStdErr(OutputRedirection.CAPTURE);
                                    r.setRunAs(textFieldRunAs.getValue().toString());

                                    String[] args = textFieldArgs.getValue().toString().split(" ");
                                    r.setArgs(Arrays.asList(args));

                                    r.setTimeout(Integer.parseInt(textFieldTimeout.getValue().toString()));

                                    Command command = new Command(r);
                                    commandManagerInterface.executeCommand(command);
                                }
                            }
                        } else {
                            getWindow().showNotification("Select agent!");
                        }
                    } catch (Exception ex) {
                        getWindow().showNotification(ex.toString());
                        System.out.println("buttonClick event Exception");
                        ex.printStackTrace();
                    }
                }
            });
            return button;
        }

        private Button genGetRequestButton() {
            Button button = new Button("Get requests");
            button.setDescription("Gets requests from Cassandra");
            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    List<Request> listofrequest = commandManagerInterface.getCommands(null);
                    StringBuilder sb = new StringBuilder();
                    for (Request request : listofrequest) {
                        sb.append(request).append("\n");
                    }
                    textAreaOutput.setValue(sb.toString());
                }
            });
            return button;
        }

        private Button genGetResponsesButton() {
            Button button = new Button("Get responses");
            button.setDescription("Gets requests from Cassandra");
            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (!Strings.isNullOrEmpty(textAreaCommand.getValue().toString())) {
                        String[] attr = textAreaCommand.getValue().toString().trim().split(" ");

                        if (attr.length == 2) {
                            try {
                                String taskUuid = attr[0];
                                int requestSequenceNumber = Integer.parseInt(attr[1]);

                                Response response = commandManagerInterface.getResponse(UUID.fromString(taskUuid), requestSequenceNumber);
                                textAreaOutput.setValue(response);
                            } catch (NumberFormatException ex) {
                                getWindow().showNotification("Enter task uuid and requestsequencenumber "
                                        + "delimited with space");
                            }
                        } else {
                            getWindow().showNotification("Enter task uuid and requestsequencenumber delimited with space");
                        }
                    } else {
                        getWindow().showNotification("Enter task uuid and requestsequencenumber delimited with space");
                    }
                }
            });
            return button;
        }

        private Button getGetTasksButton() {
            Button button = new Button("Get Tasks");
            button.setDescription("Gets tasks from Cassandra");
            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    List<Task> list = commandManagerInterface.getTasks();
                    StringBuilder sb = new StringBuilder();
                    for (Task task : list) {
                        sb.append(task).append("\n");
                    }
                    textAreaOutput.setValue(sb.toString());
                }
            });
            return button;
        }

        private Button getTruncateTablesButton() {
            Button button = new Button("Truncate tables");
            button.setDescription("Gets tasks from Cassandra");
            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (commandManagerInterface.truncateTables()) {
                        textAreaOutput.setValue("Tables truncated");
                    }
                }
            });
            return button;
        }

        private Button getClusterButton() {
            Button button = new Button("Create cluster data");
            button.setDescription("Creates Cluster Data");
            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    CassandraClusterInfo clusterData = new CassandraClusterInfo();
                    clusterData.setName(textAreaCommand.getValue().toString());
                    clusterData.setCommitLogDir("Commit log Dir");
                    clusterData.setDataDir("Data dir");
                    clusterData.setSavedCacheDir("Saved Cache Dir");

                    Set<Agent> agents = AppData.getSelectedAgentList();
                    List<UUID> listUuid = new ArrayList<UUID>();
                    for (Agent agent : agents) {
                        listUuid.add(agent.getUuid());
                    }
                    clusterData.setNodes(listUuid);
                    clusterData.setSeeds(listUuid);
                    commandManagerInterface.saveCassandraClusterData(clusterData);
                    textAreaOutput.setValue(clusterData);
                }
            });
            return button;
        }
    }

    @Override
    public String getName() {
        return Terminal.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        CommandManagerInterface commandManagerInterface = getCommandManager();
        component = new ModuleComponent(commandManagerInterface);
        commandManagerInterface.addListener(component);

        return component;
    }

    public void setModuleService(ModuleService service) {
        System.out.println("Terminal: registering with ModuleService");
        service.registerModule(this);
    }

    public void unsetModuleService(ModuleService service) {
        if (getCommandManager() != null) {
            getCommandManager().removeListener(component);
            service.unregisterModule(this);
        }
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public static CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(Terminal.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

    public static AgentManagerInterface getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(Terminal.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(AgentManagerInterface.class.getName());
            if (serviceReference != null) {
                return AgentManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
