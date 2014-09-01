package org.safehaus.subutai.ui.sqoop.manager;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import java.util.*;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.server.ui.component.*;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.ui.sqoop.SqoopUI;

public class Manager {

    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private final ImportPanel importPanel;
    private final ExportPanel exportPanel;
    private SqoopConfig config;

    public Manager() {

        contentRoot = new GridLayout();
        contentRoot.setSpacing(true);
        contentRoot.setMargin(true);
        contentRoot.setSizeFull();
        contentRoot.setRows(10);
        contentRoot.setColumns(1);

        //tables go here
        nodesTable = createTableTemplate("Nodes");
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing(true);

        Label clusterNameLabel = new Label("Select Sqoop installation:");
        controlsContent.addComponent(clusterNameLabel);

        clusterCombo = new ComboBox();
        clusterCombo.setImmediate(true);
        clusterCombo.setTextInputAllowed(false);
        clusterCombo.setWidth(200, Sizeable.Unit.PIXELS);
        clusterCombo.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                config = (SqoopConfig)event.getProperty().getValue();
                refreshUI();
            }
        });

        Button refreshClustersBtn = new Button("Refresh");
        refreshClustersBtn.addStyleName("default");
        refreshClustersBtn.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                refreshClustersInfo();
            }
        });

        controlsContent.addComponent(clusterCombo);
        controlsContent.addComponent(refreshClustersBtn);

        contentRoot.addComponent(controlsContent, 0, 0);
        contentRoot.addComponent(nodesTable, 0, 1, 0, 9);

        importPanel = new ImportPanel();
        exportPanel = new ExportPanel();

    }

    private Table createTableTemplate(String caption) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Import", Button.class, null);
        table.addContainerProperty("Export", Button.class, null);
        table.addContainerProperty("Destroy", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setSizeFull();

        table.setPageLength(10);
        table.setSelectable(true);
        table.setImmediate(true);

        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                if(event.isDoubleClick()) {
                    String lxcHostname = (String)table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = SqoopUI.getAgentManager().getAgentByHostname(lxcHostname);
                    if(lxcAgent != null) {
                        Set<Agent> set = new HashSet<>(Arrays.asList(lxcAgent));
                        TerminalWindow terminal = new TerminalWindow(set, SqoopUI.getExecutor(), SqoopUI.getCommandRunner(), SqoopUI.getAgentManager());
                        contentRoot.getUI().addWindow(terminal.getWindow());
                    } else
                        show("Agent is not connected");
                }
            }
        });
        return table;
    }

    private void refreshUI() {
        if(config != null)
            populateTable(nodesTable, config.getNodes());
        else
            nodesTable.removeAllItems();
    }

    public void refreshClustersInfo() {
        SqoopConfig current = (SqoopConfig)clusterCombo.getValue();
        clusterCombo.removeAllItems();
        List<SqoopConfig> clustersInfo = SqoopUI.getManager().getClusters();
        if(clustersInfo != null && clustersInfo.size() > 0) {
            for(SqoopConfig ci : clustersInfo) {
                clusterCombo.addItem(ci);
                clusterCombo.setItemCaption(ci, ci.getClusterName());
            }
            if(current != null)
                for(SqoopConfig ci : clustersInfo) {
                    if(ci.getClusterName().equals(current.getClusterName())) {
                        clusterCombo.setValue(ci);
                        return;
                    }
                }
        }
    }

    private void show(String notification) {
        Notification.show(notification);
    }

    private void populateTable(final Table table, Collection<Agent> agents) {

        table.removeAllItems();

        for(final Agent agent : agents) {
            final Button importBtn = new Button("Import");
            importBtn.addStyleName("default");
            final Button exportBtn = new Button("Export");
            exportBtn.addStyleName("default");
            final Button destroyBtn = new Button("Destroy");
            destroyBtn.addStyleName("default");
            final Embedded icon = new Embedded("", new ThemeResource(
                    "img/spinner.gif"));
            icon.setVisible(false);

            final List<java.io.Serializable> items = new ArrayList<>();
            items.add(agent.getHostname());
            items.add(importBtn);
            items.add(exportBtn);
            items.add(destroyBtn);
            items.add(icon);

            table.addItem(items.toArray(), null);

            importBtn.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    importPanel.setAgent(agent);
                    importPanel.setType(null);
                    SqoopUI.getForm().addTab(importPanel);
                }
            });

            exportBtn.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    exportPanel.setAgent(agent);
                    SqoopUI.getForm().addTab(exportPanel);
                }
            });

            destroyBtn.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    ConfirmationDialog alert = new ConfirmationDialog(String.format("Do you want to destroy the %s node?", agent.getHostname()),
                            "Yes", "No");
                    alert.getOk().addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            UUID trackId = SqoopUI.getManager().destroyNode(
                                    config.getClusterName(),
                                    agent.getHostname());
                            ProgressWindow window = new ProgressWindow(
                                    SqoopUI.getExecutor(), SqoopUI.getTracker(),
                                    trackId, SqoopConfig.PRODUCT_KEY);
                            window.getWindow().addCloseListener(new Window.CloseListener() {
                                @Override
                                public void windowClose(Window.CloseEvent closeEvent) {
                                    refreshClustersInfo();
                                }
                            });
                            contentRoot.getUI().addWindow(window.getWindow());
                        }
                    });

                    contentRoot.getUI().addWindow(alert.getAlert());
                }
            });

        }
    }

    public Component getContent() {
        return contentRoot;
    }

}
