package org.safehaus.subutai.ui.containermanager.clone;


import com.google.common.base.Strings;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.containermanager.ContainerManager;
import org.safehaus.subutai.api.strategymanager.ContainerPlacementStrategy;
import org.safehaus.subutai.api.strategymanager.Criteria;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.AgentUtil;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.server.ui.component.AgentTree;
import org.safehaus.subutai.ui.containermanager.executor.*;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


@SuppressWarnings("serial")
public class Cloner extends VerticalLayout implements AgentExecutionListener {
    private static final Logger LOG = Logger.getLogger(Cloner.class.getName());

    private final AgentTree agentTree;
    private final Button cloneBtn;
    private final TextField textFieldLxcName;
    private final Slider slider;
    private final ComboBox strategy;
    private final Label indicator;
    private final TreeTable lxcTable;
    private final GridLayout topContent;
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
    Map<ContainerPlacementStrategy, List<Component>> criteriaMap = new HashMap<ContainerPlacementStrategy, List<Component>>();
    List<ContainerPlacementStrategy> placementStrategies;

    public Cloner(final ContainerManager containerManager, AgentTree agentTree) {
        setSpacing(true);
        setMargin(true);

        this.agentTree = agentTree;

        this.containerManager = containerManager;


        BeanItemContainer<ContainerPlacementStrategy> container = new BeanItemContainer<ContainerPlacementStrategy>(ContainerPlacementStrategy.class);
        placementStrategies = containerManager.getPlacementStrategies();
        for (ContainerPlacementStrategy st : placementStrategies) {
            List<Component> components = new ArrayList<Component>();
            for (Criteria c : st.getCriteria()) {
                Component label = new Label(c.getTitle());
                components.add(label);
                Component checkbox = new CheckBox(c.getTitle(), (Boolean)c.getValue());
                components.add(checkbox);
            }

            criteriaMap.put(st, components);
            container.addItem(st);
        }

        textFieldLxcName = new TextField();
        slider = new Slider();
        slider.setMin(1);
        slider.setMax(20);
        slider.setWidth(200, Unit.PIXELS);
        slider.setImmediate(true);

//        Strategy defaultStrategy = new Strategy("DEFAULT", "Default placement strategy");
//        container.addItem(defaultStrategy);
//        container.addItem(new Strategy("MORE_HDD", "More HDD placement strategy"));
//        container.addItem(new Strategy("MORE_RAM", "More RAM placement strategy"));
//        container.addItem(new Strategy("MORE_CPU", "More CPU placement strategy"));


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

        topContent = new GridLayout(7, 2);
        topContent.setSpacing(true);
        strategy = new ComboBox(null, container);
        strategy.setItemCaptionPropertyId("title");
        strategy.setWidth(200, Unit.PIXELS);
        strategy.setImmediate(true);
        strategy.setTextInputAllowed(false);
        strategy.setNullSelectionAllowed(false);
        Property.ValueChangeListener listener = new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                show(event.getProperty().getValue().toString());

                Component prevComponent = topContent.getComponent(2, 4);
                if (prevComponent != null)
                    topContent.removeComponent(prevComponent);
                List<Component> criteriaComponents = criteriaMap.get(event.getProperty().getValue());

                if (criteriaComponents.size() == 0)
                    return;

                GridLayout criteriaContent = new GridLayout(criteriaComponents.size(), 2);
                criteriaContent.setSpacing(true);

                for (Component c : criteriaComponents) {
                    criteriaContent.addComponent(c);
                }

                topContent.addComponent(criteriaContent, 2, 4);
            }
        };
        strategy.addValueChangeListener(listener);
//        strategy.setValue(defaultStrategy);

        topContent.addComponent(new Label("Product name"));
        topContent.addComponent(textFieldLxcName);
        topContent.addComponent(new Label("Lxc count"));
        topContent.addComponent(slider);
        topContent.addComponent(cloneBtn);
        topContent.addComponent(clearBtn);
        topContent.addComponent(indicator);
        topContent.addComponent(new Label("Clone strategy"));
        topContent.addComponent(strategy);
        topContent.setComponentAlignment(indicator, Alignment.MIDDLE_CENTER);
        addComponent(topContent);

        lxcTable = createLxcTable("Lxc containers", 500);
        addComponent(lxcTable);
    }

    public class Strategy implements Serializable {
        private String id;
        private String name;

        public Strategy(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private void startCloneTask() {
        final String productName = textFieldLxcName.getValue().trim();

        if (Strings.isNullOrEmpty(productName)) {
            show("Please specify product name");
            return;
        }

        if (!Strings.isNullOrEmpty(productName) && !productName.matches(hostValidatorRegex)) {
            show("Please, use only letters, digits, dots and hyphens in product name");
            return;
        }

        if (strategy.getValue() == null) {
            show("Please specify placement strategy");
            return;
        }

        Set<Agent> physicalAgents = AgentUtil.filterPhysicalAgents(agentTree.getSelectedAgents());
        final Map<Agent, List<String>> agentFamilies = new HashMap<>();
        final double count = (Double) slider.getValue();
        List<Criteria> criteria = new ArrayList<Criteria>();
        if (physicalAgents.isEmpty()) { // process cloning by selected strategy

//            List<ContainerPlacementStrategy> strategies = containerManager.getPlacementStrategies();
            if (placementStrategies == null || placementStrategies.size() == 0) {
                show("There is no placement strategy.");
                return;
            }
            ContainerPlacementStrategy containerPlacementStrategy = placementStrategies.get(0);
            String placementStrategyId = containerPlacementStrategy.getId();
            if (containerPlacementStrategy.hasCriteria()) {

            }
            Map<Agent, Integer> bestServers = containerManager.getPlacementDistribution((int) count, placementStrategyId, criteria);
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
        errorProcessed = new AtomicInteger(0);
        for (final Map.Entry<Agent, List<String>> agent : agentFamilies.entrySet()) {
            AgentExecutor agentExecutor = new AgentExecutorImpl(agent.getKey().getHostname(), agent.getValue());
            agentExecutor.addListener(this);
            ExecutorService executor = Executors.newFixedThreadPool(1);
            agentExecutor.execute(executor, new CloneCommandFactory(containerManager, agent.getKey().getHostname(), "master"));
            executor.shutdown();
        }
    }

    private void startCloneTaskOld() {
        final String productName = textFieldLxcName.getValue().trim();

        if (Strings.isNullOrEmpty(productName)) {
            show("Please specify product name");
            return;
        }

        if (!Strings.isNullOrEmpty(productName) && !productName.matches(hostValidatorRegex)) {
            show("Please, use only letters, digits, dots and hyphens in product name");
            return;
        }

        if (strategy.getValue() == null) {
            show("Please specify placement strategy");
            return;
        }

        Set<Agent> physicalAgents = AgentUtil.filterPhysicalAgents(agentTree.getSelectedAgents());
        final Map<Agent, List<String>> agentFamilies = new HashMap<>();
        final double count = (Double) slider.getValue();
        List<Criteria> criteria = new ArrayList<Criteria>();
        if (physicalAgents.isEmpty()) { // process cloning by selected strategy
//            containerManager.listAllStrategies();
            Map<Agent, Integer> bestServers = null;// = containerManager.getPlacementDistribution((int)count, PlacementStrategy.ROUND_ROBIN, criteria);
//                    getPhysicalServersWithLxcSlots(((Strategy)strategy.getValue()).getId());
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
        errorProcessed = new AtomicInteger(0);
        for (final Map.Entry<Agent, List<String>> agent : agentFamilies.entrySet()) {
            AgentExecutor agentExecutor = new AgentExecutorImpl(agent.getKey().getHostname(), agent.getValue());
            agentExecutor.addListener(this);
            ExecutorService executor = Executors.newFixedThreadPool(1);
            agentExecutor.execute(executor, new CloneCommandFactory(containerManager, agent.getKey().getHostname(), "master"));
            executor.shutdown();
        }
    }

    @Override
    public void onExecutionEvent(AgentExecutionEvent event) {
        LOG.info(event.toString());
        updateContainerStatus(event);
    }

    private void updateContainerStatus(final AgentExecutionEvent event) {
        getUI().access(new Runnable() {
            @Override
            public void run() {
                Item row = lxcTable.getItem(event.getContainerName());
                if (row != null) {
                    Property p = row.getItemProperty("Status");
                    if (AgentExecutionEventType.START.equals(event.getEventType()))
                        p.setValue(new Embedded("", new ThemeResource(loadIconSource)));
                    else if (AgentExecutionEventType.SUCCESS.equals(event.getEventType())) {
                        p.setValue(new Embedded("", new ThemeResource(okIconSource)));
                        countProcessed.decrementAndGet();
                    } else if (AgentExecutionEventType.FAIL.equals(event.getEventType())) {
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
        });
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
