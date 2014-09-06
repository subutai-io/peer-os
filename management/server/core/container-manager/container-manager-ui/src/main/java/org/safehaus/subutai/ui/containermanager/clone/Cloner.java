package org.safehaus.subutai.ui.containermanager.clone;


import com.google.common.base.Strings;
import com.vaadin.data.Item;
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


@SuppressWarnings("serial")
public class Cloner extends VerticalLayout {

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

//        show(String.format("Selected physical servers count: %d", physicalAgents.size()));
        Map<Agent, List<String>> agentFamilies = new HashMap<>();
        if (physicalAgents.isEmpty()) { // process cloning by selected strategy
            final double count = (Double) slider.getValue();
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
                entry.setValue(entry.getValue() - 1);
//                bestServers.put(entry.getKey(), entry.getValue() - 1);
                List<String> lxcHostNames = agentFamilies.get(entry.getKey());
                if (lxcHostNames == null) {
                    lxcHostNames = new ArrayList<>();
                    agentFamilies.put(entry.getKey(), lxcHostNames);
                }
                lxcHostNames.add(String.format("%s%d_%s", productName, lxcHostNames.size() + 1, UUIDUtil.generateTimeBasedUUID().toString().replace('-', '_')));
            }
        } else { // process cloning in selected hosts
            double count = (Double) slider.getValue();
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
        CompletionService completionService = ContainerUI.getCompletionService();
        for (final Map.Entry<Agent, List<String>> agg : agentFamilies.entrySet()) {
            completionService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return executeAgent(agg.getKey().getHostname(), "master", agg.getValue());
                }
            });
        }

        boolean resultCumulator = true;
        for (int i = 0; i < agentFamilies.size(); i++) {
            try {
                Future<Boolean> future = completionService.take();
                resultCumulator &= future.get();
            } catch (InterruptedException | ExecutionException e) {
                resultCumulator = false;
            }
        }
        if (resultCumulator)
            show("Cloning containers finished successfully.");
        else
            show("Not all containers successfully created.");

        indicator.setVisible(false);

    }


    /**
     * Executes cloning action for agent.
     *
     * @param hostName
     * @param templateName
     * @param cloneNames
     * @return
     */
    private boolean executeAgent(final String hostName, final String templateName, List<String> cloneNames) {

        final AtomicInteger countProcessed = new AtomicInteger((int) (cloneNames.size()));
        ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            for (final String lxcHostname : cloneNames) {
                executor.execute(new Runnable() {
                    public void run() {
                        Item row = lxcTable.getItem(lxcHostname);
                        if (row != null)
                            row.getItemProperty("Status")
                                    .setValue(new Embedded("", new ThemeResource(loadIconSource)));
                        try {
                            containerManager.clone(hostName, templateName, lxcHostname);

                            if (row != null)
                                row.getItemProperty("Status")
                                        .setValue(new Embedded("", new ThemeResource(okIconSource)));
                            countProcessed.decrementAndGet();
                        } catch (ContainerCreateException ce) {
                            if (row != null)
                                row.getItemProperty("Status")
                                        .setValue(new Embedded("", new ThemeResource(errorIconSource)));
                        }
                    }
                });
            }
        } finally {
            executor.shutdown();
        }
        return countProcessed.intValue() == 0;
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
