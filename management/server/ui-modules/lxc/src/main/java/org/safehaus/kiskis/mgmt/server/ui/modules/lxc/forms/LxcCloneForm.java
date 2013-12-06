package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.forms;

import com.google.common.base.Strings;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.LxcModule;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/1/13
 * Time: 5:56 PM
 */
@SuppressWarnings("serial")
public class LxcCloneForm extends VerticalLayout implements
        Button.ClickListener {
    private static final String CLONE_LXC = "" +
            "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": \":source\",\n" +
            "\t    \"uuid\": \":uuid\",\n" +
            "\t    \"taskUuid\": \":task\",\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/lxc-clone\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"-o\",\"base-container\",\"-n\",\":lxc-host-name\"," +
            "\" && cat /dev/null > /etc/resolvconf/resolv.conf.d/original " +
            "&& cat /dev/null > /var/lib/lxc/:lxc-host-name/rootfs/etc/resolvconf/resolv.conf.d/original\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    private Set<Agent> physicalAgents;
    private Task cloneTask;
    private TextField textFieldLxcName;
    private Button buttonClone;
    private Panel outputPanel;

    public LxcCloneForm() {
        setSpacing(true);
        // Panel 1 - with caption
        Panel panel = new Panel("Clone LXC template");
        textFieldLxcName = new TextField("Clone LXC Template");
        buttonClone = new Button("Clone");
        buttonClone.addListener(this);

        HorizontalLayout hLayout = new HorizontalLayout();

        hLayout.addComponent(textFieldLxcName);
        hLayout.addComponent(buttonClone);
        hLayout.setComponentAlignment(textFieldLxcName, Alignment.BOTTOM_CENTER);
        hLayout.setComponentAlignment(buttonClone, Alignment.BOTTOM_CENTER);
        panel.addComponent(hLayout);

        VerticalLayout layout = (VerticalLayout) panel.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);

        outputPanel = new Panel("Clone command output");

        addComponent(panel);
        addComponent(outputPanel);
    }

    @Override
    public void buttonClick(Button.ClickEvent clickEvent) {
        Set<Agent> agents = AppData.getSelectedAgentList();
        if (agents != null && agents.size() > 0) {
            physicalAgents = new HashSet<Agent>();
            for (Agent agent : agents) {
                if (!agent.isIsLXC()) {
                    physicalAgents.add(agent);
                }
            }

            if (physicalAgents.size() == 0) {
                getWindow().showNotification("Select at least one physical agent");
            } else if (Strings.isNullOrEmpty(textFieldLxcName.getValue().toString())) {
                getWindow().showNotification("Enter lxc hostname");
            } else {
                outputPanel.removeAllComponents();
                createTask();
            }
        }
    }

    private void createTask() {
        cloneTask = new Task();
        cloneTask.setTaskStatus(TaskStatus.NEW);
        cloneTask.setDescription("Cloning lxc container");
        if (getCommandManager() != null) {
            getCommandManager().saveTask(cloneTask);
            createRequests();
        }
    }

    private void createRequests() {
        String jsonTemplate = CLONE_LXC;
        jsonTemplate = jsonTemplate.replaceAll(":task", cloneTask.getUuid().toString());
        jsonTemplate = jsonTemplate.replaceAll(":source", LxcModule.MODULE_NAME);

        for (Agent agent : physicalAgents) {
            String json = jsonTemplate.replaceAll(":uuid", agent.getUuid().toString());
            json = json.replaceAll(":requestSequenceNumber", cloneTask.getIncrementedReqSeqNumber().toString());
            json = json.replaceAll(":lxc-host-name",
                    agent.getHostname() + Common.PARENT_CHILD_LXC_SEPARATOR + textFieldLxcName.getValue().toString());

            Request request = CommandJson.getRequest(json);
            if (getCommandManager() != null) {
                getCommandManager().executeCommand(new Command(request));
            }

            buttonClone.setEnabled(false);
        }
    }

    public void outputResponse(Response response) {
        if (cloneTask != null && response.getTaskUuid().compareTo(cloneTask.getUuid()) == 0) {
            setTaskStatus();
        }
    }

    public void setTaskStatus() {
        if (getCommandManager() != null) {
            List<ParseResult> result = getCommandManager().parseTask(cloneTask, true);
            for (ParseResult pr : result) {
                if (pr.getResponse().getType().equals(ResponseType.EXECUTE_RESPONSE_DONE)) {
                    if (pr.getResponse().getExitCode() == 0) {
                        Label labelOk = new Label(pr.getResponse().getStdOut());
                        labelOk.setIcon(new ThemeResource("icons/16/ok.png"));
                        outputPanel.addComponent(labelOk);

                        buttonClone.setEnabled(true);
                    } else {
                        Label labelError = new Label(pr.getResponse().getStdOut() + " " + pr.getResponse().getStdErr());
                        labelError.setIcon(new ThemeResource("icons/16/cancel.png"));
                        outputPanel.addComponent(labelError);

                        buttonClone.setEnabled(true);
                    }
                } else if (pr.getResponse().getType().equals(ResponseType.EXECUTE_TIMEOUTED)) {
                    Label labelError = new Label(pr.getResponse().getStdOut() + " " + pr.getResponse().getStdErr());
                    labelError.setIcon(new ThemeResource("icons/16/cancel.png"));
                    outputPanel.addComponent(labelError);

                    buttonClone.setEnabled(true);
                }
            }
        }
    }

    public CommandManagerInterface getCommandManager() {
        BundleContext ctx = FrameworkUtil.getBundle(LxcModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
