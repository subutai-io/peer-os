package org.safehaus.subutai.ui.containermanager.clone;


import com.google.common.base.Strings;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.containermanager.ContainerCreateException;
import org.safehaus.subutai.api.containermanager.ContainerManager;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.AgentUtil;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.server.ui.component.AgentTree;
import org.safehaus.subutai.ui.containermanager.ContainerUI;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


@SuppressWarnings("serial")
public class Cloner extends VerticalLayout implements AgentExecutionListener {
    private static final Logger LOG = Logger.getLogger(Cloner.class.getName());

    private final AgentTree agentTree;
    private final Button cloneBtn;
    private final TextField textFieldLxcName;
    private final Slider slider;
    private final Label indicator;
    private final TreeTable lxcTable;
    private final ContainerManager containerManager;
    private final String physicalHostLabel = "Physical Host";
    private final String statusLabel = "Status";
    private final String okIconSource = "img/ok.png";
    private final String errorIconSource = "img/cancel.png";
    private final String loadIconSource = "img/spinner.gif";
    private final String hostValidatorRegex =
            "^(?=.{1,255}$)[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?(?:\\.[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,"
                    + "61}[0-9A-Za-z])?)*\\.?$";
    AtomicInteger countProcessed = null;
    AtomicInteger errorProcessed = null;


    public Cloner(final ContainerManager containerManager, AgentTree agentTree) {
        setSpacing(true);
        setMargin(true);

        this.agentTree = agentTree;

        this.containerManager = containerManager;

        textFieldLxcName = new TextField();
        slider = new Slider();
        slider.setMin(1);
        slider.setMax(20);
        slider.setWidth(150, Unit.PIXELS);
        slider.setImmediate(true);
        cloneBtn = new Button("Clone");
        cloneBtn.addStyleName("default");
        cloneBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                startCloneTask();
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.addStyleName("default");
        clearBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                //clear completed
                for (Object rowId : lxcTable.getItemIds()) {
                    Item row = lxcTable.getItem(rowId);
                    if (row != null) {
                        Embedded statusIcon = (Embedded) (row.getItemProperty(statusLabel).getValue());
                        if (statusIcon != null && (
                                okIconSource.equals(((ThemeResource) statusIcon.getSource()).getResourceId())
                                        || errorIconSource
                                        .equals(((ThemeResource) statusIcon.getSource()).getResourceId()))) {
                            lxcTable.removeItem(rowId);
                        }
                    }
                }
                //clear empty parents
                for (Object rowId : lxcTable.getItemIds()) {
                    Item row = lxcTable.getItem(rowId);
                    if (row != null && row.getItemProperty(physicalHostLabel).getValue() != null && (
                            lxcTable.getChildren(rowId) == null || lxcTable.getChildren(rowId).isEmpty())) {
                        lxcTable.removeItem(rowId);
                    }
                }
            }
        });

        indicator = new Label();
        indicator.setIcon(new ThemeResource("img/spinner.gif"));
        indicator.setContentMode(ContentMode.HTML);
        indicator.setHeight(11, Unit.PIXELS);
        indicator.setWidth(50, Unit.PIXELS);
        indicator.setVisible(false);

        GridLayout topContent = new GridLayout(7, 1);
        topContent.setSpacing(true);

        topContent.addComponent(new Label("Product name"));
        topContent.addComponent(textFieldLxcName);
        topContent.addComponent(new Label("Lxc count"));
        topContent.addComponent(slider);
        topContent.addComponent(cloneBtn);
        topContent.addComponent(clearBtn);
        topContent.addComponent(indicator);
        topContent.setComponentAlignment(indicator, Alignment.MIDDLE_CENTER);
        addComponent(topContent);

        lxcTable = createLxcTable("Lxc containers", 500);
        addComponent(lxcTable);
    }

    private void startCloneTask() {
        Set<Agent> physicalAgents = AgentUtil.filterPhysicalAgents(agentTree.getSelectedAgents());
        final String productName = textFieldLxcName.getValue().trim();

        if (!Strings.isNullOrEmpty(productName) && !productName.matches(hostValidatorRegex)) {
            show("Please, use only letters, digits, dots and hyphens in product name");
            return;
        }

        final Map<Agent, List<String>> agentFamilies = new HashMap<>();
        final double count = (Double) slider.getValue();
        if (physicalAgents.isEmpty()) { // process cloning by selected strategy

            Map<Agent, Integer> bestServers = containerManager.getPhysicalServersWithLxcSlots();
            if (bestServers.isEmpty()) {
                show("No servers available to accommodate new lxc containers");
                return;
            }
            int numOfLxcSlots = 0;
            for (Map.Entry<Agent, Integer> srv : bestServers.entrySet()) {
                numOfLxcSlots += srv.getValue();
            }

            if (numOfLxcSlots < count) {
                show(String.format("Only %s lxc containers can be created", numOfLxcSlots));
                return;
            }

            for (int i = 1; i <= count; i++) {
                Map<Agent, Integer> sortedBestServers =
                        CollectionUtil.sortMapByValueDesc(bestServers);
                final Map.Entry<Agent, Integer> entry = sortedBestServers.entrySet().iterator().next();
                bestServers.put(entry.getKey(), entry.getValue() - 1);
                List<String> lxcHostNames = agentFamilies.get(entry.getKey());
                if (lxcHostNames == null) {
                    lxcHostNames = new ArrayList<>();
                    agentFamilies.put(entry.getKey(), lxcHostNames);
                }
                lxcHostNames.add(String.format("%s%d_%s", productName, lxcHostNames.size() + 1, UUIDUtil.generateTimeBasedUUID().toString().replace('-', '_')));
            }
        } else { // process cloning in selected hosts
            for (Agent physAgent : physicalAgents) {
                List<String> lxcHostNames = new ArrayList<>();
                for (int i = 1; i <= count; i++) {
                    lxcHostNames.add(String.format("%s%d_%s", productName, lxcHostNames.size() + 1, UUIDUtil.generateTimeBasedUUID().toString().replace('-', '_')));
                }
                agentFamilies.put(physAgent, lxcHostNames);
            }
        }

        indicator.setVisible(true);
        populateLxcTable(agentFamilies);
        countProcessed = new AtomicInteger((int) (count));
        errorProcessed = new AtomicInteger((int) (0));
        final Cloner self = this;
        ContainerUI.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                for (final Map.Entry<Agent, List<String>> agg : agentFamilies.entrySet()) {
//                    executors.add(executeAgent(agg.getKey().getHostname(), "master", agg.getValue()));
                    AgentExecutor agentExecutor = new AgentExecutor(agg.getKey().getHostname(), "master", agg.getValue());
                    agentExecutor.addListener(self);
                    agentExecutor.execute(Executors.newFixedThreadPool(1));
                }
            }
        });
//
//        boolean resultCumulator = true;
//        for (int i = 0; i < agentFamilies.size(); i++) {
//            try {
//                Future<Boolean> future = completionService.take();
//                resultCumulator &= future.get();
//            } catch (InterruptedException | ExecutionException e) {
//                resultCumulator = false;
//            }
//        }
//        if (resultCumulator)
//            show("Cloning containers finished successfully.");
//        else
//            show("Not all containers successfully created.");
//
//        indicator.setVisible(false);

    }


    /**
     * Executes cloning action for agent.
     *
     * @param hostName
     * @param templateName
     * @param cloneNames
     * @return
     */
    private ExecutorService executeAgent(final String hostName, final String templateName, List<String> cloneNames) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        for (final String lxcHostname : cloneNames) {
            executor.execute(new Runnable() {
                public void run() {
                    updateContainerStatus(lxcHostname, new ThemeResource(loadIconSource));
                    try {
                        containerManager.clone(hostName, templateName, lxcHostname);
                        updateContainerStatus(lxcHostname, new ThemeResource(okIconSource));

                    } catch (ContainerCreateException ce) {
                        updateContainerStatus(lxcHostname, new ThemeResource(errorIconSource));
                        errorProcessed.incrementAndGet();
                    }
                }
            });
        }
        return executor;
    }

    @Override
    public void onExecutionEvent(AgentExecutionEvent event) {
        LOG.info(event.toString());
        updateContainerStatus(event);
    }

    private class AgentExecutor {
        private String hostName;
        private String templateName;
        private List<String> cloneNames;
        private CompletionService<AgentExecutionEvent> completionService;
        private ExecutorService executor;
        private List<AgentExecutionListener> listeners = new ArrayList<>();
        ExecutorService waiter = Executors.newFixedThreadPool(1);

        public AgentExecutor(String hostName, String templateName, List<String> cloneNames) {
            this.hostName = hostName;
            this.templateName = templateName;
            this.cloneNames = cloneNames;
        }

        public void addListener(org.safehaus.subutai.ui.containermanager.clone.AgentExecutionListener listener) {
            if (listener != null)
                listeners.add(listener);
        }

        public void execute(final ExecutorService executor) {
            this.executor = executor;
            completionService = new ExecutorCompletionService(executor);
            for (final String lxcHostname : cloneNames) {
                completionService.submit(new Callable() {
                    public AgentExecutionEvent call() {
                        fireEvent(new AgentExecutionEvent(hostName, lxcHostname, "STARTED", ""));
                        try {
                            containerManager.clone(hostName, templateName, lxcHostname);
                            return (new AgentExecutionEvent(hostName, lxcHostname, "SUCCESS", ""));
                        } catch (ContainerCreateException ce) {
                            return (new AgentExecutionEvent(hostName, lxcHostname, "FAIL", ce.toString()));
                        }
//                        return new AgentExecutionEvent(hostName, lxcHostname, "FINISH", "");
                    }
                });
            }

            executor.shutdown();
            waiter.execute(new Runnable() {
                @Override
                public void run() {
                    for (String cn : cloneNames) {
                        try {
                            Future<AgentExecutionEvent> future = completionService.take();
                            AgentExecutionEvent result = future.get();
                            fireEvent(result);
                        } catch (InterruptedException | ExecutionException e) {
                            fireEvent(new AgentExecutionEvent(hostName, "", "ERROR", e.toString()));
                        }
                    }
                }
            });
            waiter.shutdown();
        }

        private void fireEvent(AgentExecutionEvent event) {
            for (AgentExecutionListener listener : listeners) {
                listener.onExecutionEvent(event);
            }
        }
    }

    /**
     * Executes cloning action for agent.
     *
     * @param hostName
     * @param templateName
     * @param cloneNames
     * @return
     */
    private void executeAgentOld(final String hostName, final String templateName, List<String> cloneNames) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        for (final String lxcHostname : cloneNames) {
            executor.execute(new Runnable() {
                ExecutorService executor;

                public void run() {
                    updateContainerStatus(lxcHostname, new ThemeResource(loadIconSource));
                    try {
                        containerManager.clone(hostName, templateName, lxcHostname);
                        updateContainerStatus(lxcHostname, new ThemeResource(okIconSource));

                    } catch (ContainerCreateException ce) {
                        updateContainerStatus(lxcHostname, new ThemeResource(errorIconSource));
                        errorProcessed.incrementAndGet();
                    }
                    int i = countProcessed.decrementAndGet();
                    if (i == 0) {
                        indicator.setVisible(false);
                    }

                }
            });
        }
    }

    private void updateContainerStatus(final String lxcHostname, final ThemeResource resource) {
        getUI().access(new Runnable() {
            @Override
            public void run() {
                Item row = lxcTable.getItem(lxcHostname);
                if (row != null) {
                    Property p = row.getItemProperty("Status");
                    p.setValue(new Embedded("", resource));

                }
            }
        });
    }


    private void updateContainerStatus(AgentExecutionEvent event) {
        Item row = lxcTable.getItem(event.getContainerName());
        if (row != null) {
            Property p = row.getItemProperty("Status");
            if ("STARTED".equals(event.getEventType()))
                p.setValue(new Embedded("", new ThemeResource(loadIconSource)));
            else if ("SUCCESS".equals(event.getEventType())) {
                p.setValue(new Embedded("", new ThemeResource(okIconSource)));
                countProcessed.decrementAndGet();
            } else if ("FAIL".equals(event.getEventType())) {
                p.setValue(new Embedded("", new ThemeResource(errorIconSource)));
                countProcessed.decrementAndGet();
                errorProcessed.incrementAndGet();
            }
        }

        if (countProcessed.intValue() == 0) {
            indicator.setVisible(false);
            if (errorProcessed.intValue() == 0)
                show("Cloning containers finished successfully.");
            else
                show("Not all containers successfully created.");

        }
    }

    private TreeTable createLxcTable(String caption, int size) {
        TreeTable table = new TreeTable(caption);
        table.addContainerProperty(physicalHostLabel, String.class, null);
        table.addContainerProperty("Lxc Host", String.class, null);
        table.addContainerProperty(statusLabel, Embedded.class, null);
        table.setWidth(100, Unit.PERCENTAGE);
        table.setHeight(size, Unit.PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);
        return table;
    }


    private void show(String msg) {
        Notification.show(msg);
    }


    private void populateLxcTable(Map<Agent, List<String>> agents) {

        for (final Agent agent : agents.keySet()) {
            if (lxcTable.getItem(agent.getHostname()) == null) {
                lxcTable.addItem(new Object[]{agent.getHostname(), null, null}, agent.getHostname());
            }
            lxcTable.setCollapsed(agent.getHostname(), false);
            for (String lxc : agents.get(agent)) {
                Embedded progressIcon = new Embedded("", new ThemeResource(loadIconSource));

                lxcTable.addItem(new Object[]{
                        null, lxc, null /*progressIcon*/
                }, lxc);

                lxcTable.setParent(lxc, agent.getHostname());
                lxcTable.setChildrenAllowed(lxc, false);
            }
        }
    }
}
