/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.solr.manager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.enums.NodeState;
import org.safehaus.subutai.ui.solr.SolrUI;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author dilshat
 */
public class Manager {

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private Config config;

    public Manager() {

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(100, Sizeable.Unit.PERCENTAGE);
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
//        clusterCombo.setMultiSelect(false);
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

        controlsContent.addComponent(clusterCombo);

        Button refreshClustersBtn = new Button("Refresh clusters");
        refreshClustersBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                refreshClustersInfo();
            }
        });

        controlsContent.addComponent(refreshClustersBtn);

        Button checkAllBtn = new Button("Check all");
        checkAllBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                checkNodesStatus(nodesTable);
            }
        });
        controlsContent.addComponent(checkAllBtn);

        Button startAllBtn = new Button("Start all");
        startAllBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                startAllNodes(nodesTable);
            }
        });
        controlsContent.addComponent(startAllBtn);

        Button stopAllBtn = new Button("Stop all");
        stopAllBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                stopAllNodes(nodesTable);
            }
        });
        controlsContent.addComponent(stopAllBtn);

        Button destroyClusterBtn = new Button("Destroy cluster");
        destroyClusterBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                if (config != null) {
                    ConfirmationDialog alert = new ConfirmationDialog(String.format("Do you want to destroy the %s cluster?", config.getClusterName()),
                            "Yes", "No");
                    alert.getOk().addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            UUID trackID = SolrUI.getSolrManager().uninstallCluster(config.getClusterName());
                            ProgressWindow window = new ProgressWindow(SolrUI.getExecutor(), SolrUI.getTracker(), trackID, Config.PRODUCT_KEY);
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
                } else {
                    show("Please, select cluster");
                }
            }
        });

        controlsContent.addComponent(destroyClusterBtn);

        Button addNodeBtn = new Button("Add Node");

        addNodeBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                if (config != null) {
                    ConfirmationDialog alert = new ConfirmationDialog(String.format("Do you want to add node to the %s cluster?", config.getClusterName()),
                            "Yes", "No");
                    alert.getOk().addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            UUID trackID = SolrUI.getSolrManager().addNode(config.getClusterName());
                            ProgressWindow window = new ProgressWindow(SolrUI.getExecutor(), SolrUI.getTracker(), trackID, Config.PRODUCT_KEY);
                            window.getWindow().addCloseListener(new Window.CloseListener() {
                                @Override
                                public void windowClose(Window.CloseEvent closeEvent) {
                                    refreshClustersInfo();
                                }
                            });
                            contentRoot.getUI().addWindow(window.getWindow());
                        }
                    });

                    try {
                        contentRoot.getUI().getSession().getCurrent().getLockInstance().lock();
                        contentRoot.getUI().addWindow(alert.getAlert());
                    } finally {
                        contentRoot.getUI().getSession().getCurrent().getLockInstance().unlock();
                    }
                } else {
                    show("Please, select cluster");
                }
            }
        });

        controlsContent.addComponent(addNodeBtn);

        content.addComponent(controlsContent);

        content.addComponent(nodesTable);

    }

    public static void checkNodesStatus(Table table) {
        for (Object o : table.getItemIds()) {
            int rowId = (Integer) o;
            Item row = table.getItem(rowId);
            Button checkBtn = (Button) (row.getItemProperty("Check").getValue());
            checkBtn.click();
        }
    }

    public static void startAllNodes(Table table) {
        for (Object o : table.getItemIds()) {
            int rowId = (Integer) o;
            Item row = table.getItem(rowId);
            Button checkBtn = (Button) (row.getItemProperty("Start").getValue());
            checkBtn.click();
        }
    }

    public static void stopAllNodes(Table table) {
        for (Object o : table.getItemIds()) {
            int rowId = (Integer) o;
            Item row = table.getItem(rowId);
            Button checkBtn = (Button) (row.getItemProperty("Stop").getValue());
            checkBtn.click();
        }
    }

    public Component getContent() {
        return contentRoot;
    }

    private void show(String notification) {
        Notification.show(notification);
    }

    private void populateTable(final Table table, Set<Agent> agents) {

        table.removeAllItems();

        for (final Agent agent : agents) {
            final Button checkBtn = new Button("Check");
            final Button startBtn = new Button("Start");
            final Button stopBtn = new Button("Stop");
            final Button destroyBtn = new Button("Destroy");
            final Embedded progressIcon = new Embedded("", new ThemeResource("img/spinner.gif"));
            stopBtn.setEnabled(false);
            startBtn.setEnabled(false);
            progressIcon.setVisible(false);

            table.addItem(new Object[]{
                            agent.getHostname(),
                            checkBtn,
                            startBtn,
                            stopBtn,
                            destroyBtn,
                            progressIcon},
                    null
            );

            checkBtn.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    progressIcon.setVisible(true);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    SolrUI.getExecutor().execute(new CheckTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
                                if (state == NodeState.RUNNING) {
                                    stopBtn.setEnabled(true);
                                } else if (state == NodeState.STOPPED) {
                                    startBtn.setEnabled(true);
                                }
                                destroyBtn.setEnabled(true);
                                progressIcon.setVisible(false);
                            }
                        }
                    }));
                }
            });

            startBtn.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    progressIcon.setVisible(true);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    SolrUI.getExecutor().execute(new StartTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
                                if (state == NodeState.RUNNING) {
                                    stopBtn.setEnabled(true);
                                } else {
                                    startBtn.setEnabled(true);
                                }
                                destroyBtn.setEnabled(true);
                                progressIcon.setVisible(false);
                            }
                        }
                    }));
                }
            });

            stopBtn.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    progressIcon.setVisible(true);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    SolrUI.getExecutor().execute(new StopTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
                                if (state == NodeState.STOPPED) {
                                    startBtn.setEnabled(true);
                                } else {
                                    stopBtn.setEnabled(true);
                                }
                                destroyBtn.setEnabled(true);
                                progressIcon.setVisible(false);
                            }
                        }
                    }));
                }
            });

            destroyBtn.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format("Do you want to destroy the %s node?", agent.getHostname()),
                            "Yes", "No");
                    alert.getOk().addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            UUID trackID = SolrUI.getSolrManager().destroyNode(config.getClusterName(), agent.getHostname());
                            final ProgressWindow window = new ProgressWindow(SolrUI.getExecutor(), SolrUI.getTracker(), trackID, Config.PRODUCT_KEY);
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
        List<Config> mongoClusterInfos = SolrUI.getSolrManager().getClusters();
        Config clusterInfo = (Config) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if (mongoClusterInfos != null && mongoClusterInfos.size() > 0) {
            for (Config mongoClusterInfo : mongoClusterInfos) {
                clusterCombo.addItem(mongoClusterInfo);
                clusterCombo.setItemCaption(mongoClusterInfo,
                        mongoClusterInfo.getClusterName());
            }
            if (clusterInfo != null) {
                for (Config mongoClusterInfo : mongoClusterInfos) {
                    if (mongoClusterInfo.getClusterName().equals(clusterInfo.getClusterName())) {
                        clusterCombo.setValue(mongoClusterInfo);
                        return;
                    }
                }
            } else {
                clusterCombo.setValue(mongoClusterInfos.iterator().next());
            }
        }
    }

    private Table createTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Start", Button.class, null);
        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Destroy", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.Unit.PERCENTAGE);
        table.setHeight(size, Sizeable.Unit.PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = SolrUI.getAgentManager().getAgentByHostname(lxcHostname);
                    if (lxcAgent != null) {
                        TerminalWindow terminal = new TerminalWindow(Util.wrapAgentToSet(lxcAgent), SolrUI.getExecutor(), SolrUI.getCommandRunner(), SolrUI.getAgentManager());
                        contentRoot.getUI().addWindow(terminal.getWindow());
                    } else {
                        show("Agent is not connected");
                    }
                }
            }
        });
        return table;
    }

}
