package org.safehaus.kiskis.mgmt.ui.sqoop.manager;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.sqoop.Config;
import org.safehaus.kiskis.mgmt.server.ui.ConfirmationDialogCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.sqoop.SqoopUI;

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
        contentRoot.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        VerticalLayout content = new VerticalLayout();
        content.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);

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
        clusterCombo.setMultiSelect(false);
        clusterCombo.setImmediate(true);
        clusterCombo.setTextInputAllowed(false);
        clusterCombo.setWidth(200, Sizeable.UNITS_PIXELS);
        clusterCombo.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                config = (Config) event.getProperty().getValue();
                refreshUI();
            }
        });

        Button refreshClustersBtn = new Button("Refresh clusters");
        refreshClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                refreshClustersInfo();
            }
        });

        Button destroyClusterBtn = new Button("Destroy cluster");
        destroyClusterBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config == null) {
                    show("Select cluster");
                    return;
                }
                MgmtApplication.showConfirmationDialog("Destroy cluster",
                        String.format("Cluster '%s' will be destroyed. Continue?",
                                config.getClusterName()), "Yes", "Cancel",
                        new ConfirmationDialogCallback() {

                            public void response(boolean ok) {
                                if (ok) destroyClusterHandler();
                            }
                        }
                );
            }

        });

        Button addNodeBtn = new Button("Add Node");
        addNodeBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config == null) {
                    show("Select cluster");
                    return;
                }

                org.safehaus.kiskis.mgmt.api.hadoop.Config hci = SqoopUI.getHadoopManager().getCluster(config.getClusterName());
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

                AddNodeWindow w = new AddNodeWindow(config, set);
                MgmtApplication.addCustomWindow(w);
                w.addListener(new Window.CloseListener() {

                    public void windowClose(Window.CloseEvent e) {
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
        contentRoot.getWindow().showNotification(notification);
    }

    private void populateTable(final Table table, Collection<Agent> agents) {

        table.removeAllItems();

        for (final Agent agent : agents) {
            final Button importBtn = new Button("Import");
            final Button exportBtn = new Button("Export");
            final Button destroyBtn = new Button("Destroy");
            final Embedded icon = new Embedded("", new ThemeResource(
                    "../base/common/img/loading-indicator.gif"));
            icon.setVisible(false);

            final List items = new ArrayList();
            items.add(agent.getHostname());
            items.add(importBtn);
            items.add(exportBtn);
            items.add(destroyBtn);
            items.add(icon);

            table.addItem(items.toArray(), null);

            importBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    importPanel.setAgent(agent);
                    importPanel.setType(null);
                    SqoopUI.getForm().addTab(importPanel);
                }
            });

            exportBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    exportPanel.setAgent(agent);
                    SqoopUI.getForm().addTab(exportPanel);
                }
            });

            destroyBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    MgmtApplication.showConfirmationDialog(
                            "Destroy node",
                            String.format("Do you want to destroy node %s?", agent.getHostname()),
                            "Yes", "Cancel", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (!ok) return;
                                    UUID trackID = SqoopUI.getManager().destroyNode(
                                            config.getClusterName(),
                                            agent.getHostname());
                                    MgmtApplication.showProgressWindow(
                                            Config.PRODUCT_KEY, trackID,
                                            new Window.CloseListener() {

                                                public void windowClose(Window.CloseEvent e) {
                                                    refreshClustersInfo();
                                                }
                                            }
                                    );
                                }
                            }
                    );

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
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);

        table.setPageLength(10);
        table.setSelectable(true);
        table.setImmediate(true);

        table.addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String hostname = (String) table.getItem(event.getItemId())
                            .getItemProperty("Host").getValue();
                    Agent agent = SqoopUI.getAgentManager().getAgentByHostname(hostname);
                    if (agent != null) {
                        Window terminal = MgmtApplication.createTerminalWindow(Util.wrapAgentToSet(agent));
                        MgmtApplication.addCustomWindow(terminal);
                    } else {
                        show("Agent is not connected");
                    }
                }
            }
        });
        return table;
    }

    private void destroyClusterHandler() {

        UUID trackId = SqoopUI.getManager().uninstallCluster(config.getClusterName());

        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackId,
                new Window.CloseListener() {

                    public void windowClose(Window.CloseEvent e) {
                        refreshClustersInfo();
                    }
                }
        );
    }
}
