package org.safehaus.subutai.ui.sqoop.manager;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.sqoop.Config;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.ui.sqoop.SqoopUI;

import java.util.*;

public class Manager {

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private final ImportPanel importPanel;
    private final ExportPanel exportPanel;
    private Config config;

    public Manager() {

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(90, Sizeable.Unit.PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.Unit.PERCENTAGE);

        VerticalLayout content = new VerticalLayout();
        content.setWidth(100, Sizeable.Unit.PERCENTAGE);
        content.setHeight(100, Sizeable.Unit.PERCENTAGE);

        contentRoot.addComponent(content);
        contentRoot.setComponentAlignment(content, Alignment.TOP_CENTER);
        contentRoot.setMargin(true);

        //tables go here
        nodesTable = createTableTemplate("Nodes", 200);
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing(true);

        Label clusterNameLabel = new Label("Select the cluster");
        controlsContent.addComponent(clusterNameLabel);

        clusterCombo = new ComboBox();
        clusterCombo.setImmediate(true);
        clusterCombo.setTextInputAllowed(false);
        clusterCombo.setWidth(200, Sizeable.Unit.PIXELS);
        clusterCombo.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                config = (Config) event.getProperty().getValue();
                refreshUI();
            }
        });

        Button refreshClustersBtn = new Button("Refresh clusters");
        refreshClustersBtn.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                refreshClustersInfo();
            }
        });

        Button destroyClusterBtn = new Button("Destroy cluster");
        destroyClusterBtn.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config == null) {
                    show("Select cluster");
                    return;
                }

                ConfirmationDialog alert = new ConfirmationDialog(String.format("Do you want to destroy the %s cluster?", config.getClusterName()),
                        "Yes", "No");
                alert.getOk().addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        destroyClusterHandler();
                    }
                });

                contentRoot.getUI().addWindow(alert.getAlert());
            }

        });

        Button addNodeBtn = new Button("Add Node");
        addNodeBtn.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config == null) {
                    show("Select cluster");
                    return;
                }

                org.safehaus.subutai.api.hadoop.Config hci = SqoopUI.getHadoopManager().getCluster(config.getClusterName());
                if (hci == null) {
                    show("Hadoop cluster info not found");
                    return;
                }

                Set<Agent> set = new HashSet<Agent>(hci.getAllNodes());
                set.removeAll(config.getNodes());
                if (set.isEmpty()) {
                    show("All nodes in Hadoop cluster have Sqoop installed");
                    return;
                }

                AddNodeWindow addNodeWindow = new AddNodeWindow(config, set);
                contentRoot.getUI().addWindow(addNodeWindow);
                addNodeWindow.addCloseListener(new Window.CloseListener() {
                    @Override
                    public void windowClose(Window.CloseEvent closeEvent) {
                        refreshClustersInfo();
                    }
                });
            }
        });

        controlsContent.addComponent(clusterCombo);
        controlsContent.addComponent(refreshClustersBtn);
        controlsContent.addComponent(destroyClusterBtn);
        controlsContent.addComponent(addNodeBtn);

        content.addComponent(controlsContent);
        content.addComponent(nodesTable);

        importPanel = new ImportPanel();
        exportPanel = new ExportPanel();

    }

    public Component getContent() {
        return contentRoot;
    }

    private void show(String notification) {
        Notification.show(notification);
    }

    private void populateTable(final Table table, Collection<Agent> agents) {

        table.removeAllItems();

        for (final Agent agent : agents) {
            final Button importBtn = new Button("Import");
            final Button exportBtn = new Button("Export");
            final Button destroyBtn = new Button("Destroy");
            final Embedded icon = new Embedded("", new ThemeResource(
                    "img/spinner.gif"));
            icon.setVisible(false);

            final List items = new ArrayList();
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
                            UUID trackID = SqoopUI.getManager().destroyNode(
                                    config.getClusterName(),
                                    agent.getHostname());
                            ProgressWindow window = new ProgressWindow(SqoopUI.getExecutor(), SqoopUI.getTracker(), trackID, Config.PRODUCT_KEY);
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

    private void refreshUI() {
        if (config != null) {
            populateTable(nodesTable, config.getNodes());
        } else {
            nodesTable.removeAllItems();
        }
    }

    public void refreshClustersInfo() {
        Config current = (Config) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        List<Config> clustersInfo = SqoopUI.getManager().getClusters();
        if (clustersInfo != null && clustersInfo.size() > 0) {
            for (Config ci : clustersInfo) {
                clusterCombo.addItem(ci);
                clusterCombo.setItemCaption(ci, ci.getClusterName());
            }
            if (current != null) {
                for (Config ci : clustersInfo) {
                    if (ci.getClusterName().equals(current.getClusterName())) {
                        clusterCombo.setValue(ci);
                        return;
                    }
                }
            }
        }
    }

    private Table createTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Import", Button.class, null);
        table.addContainerProperty("Export", Button.class, null);
        table.addContainerProperty("Destroy", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.Unit.PERCENTAGE);
        table.setHeight(size, Sizeable.Unit.PIXELS);

        table.setPageLength(10);
        table.setSelectable(true);
        table.setImmediate(true);

        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = SqoopUI.getAgentManager().getAgentByHostname(lxcHostname);
                    if (lxcAgent != null) {
                        TerminalWindow terminal = new TerminalWindow(Util.wrapAgentToSet(lxcAgent), SqoopUI.getExecutor(), SqoopUI.getCommandRunner(), SqoopUI.getAgentManager());
                        contentRoot.getUI().addWindow(terminal.getWindow());
                    } else {
                        show("Agent is not connected");
                    }
                }
            }
        });
        return table;
    }

    private void destroyClusterHandler() {

        UUID trackID = SqoopUI.getManager().uninstallCluster(config.getClusterName());

        ProgressWindow window = new ProgressWindow(SqoopUI.getExecutor(), SqoopUI.getTracker(), trackID, Config.PRODUCT_KEY);
        window.getWindow().addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent closeEvent) {
                refreshClustersInfo();
            }
        });
        contentRoot.getUI().addWindow(window.getWindow());
    }
}
