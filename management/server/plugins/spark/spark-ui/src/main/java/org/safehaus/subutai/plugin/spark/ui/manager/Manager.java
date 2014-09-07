package org.safehaus.subutai.plugin.spark.ui.manager;

import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.ui.SparkUI;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

public class Manager {

    public static final String MASTER_PREFIX = "Master: ";
    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private SparkClusterConfig config;

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

        Label clusterNameLabel = new Label("Select the cluster");
        controlsContent.addComponent(clusterNameLabel);

        clusterCombo = new ComboBox();
        clusterCombo.setImmediate(true);
        clusterCombo.setTextInputAllowed(false);
        clusterCombo.setWidth(200, Sizeable.Unit.PIXELS);
        clusterCombo.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                config = (SparkClusterConfig)event.getProperty().getValue();
                refreshUI();
            }
        });

        controlsContent.addComponent(clusterCombo);

        Button refreshClustersBtn = new Button("Refresh clusters");
        refreshClustersBtn.addStyleName("default");
        refreshClustersBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                refreshClustersInfo();
            }
        });
        controlsContent.addComponent(refreshClustersBtn);

        Button checkAllBtn = new Button("Check All");
        checkAllBtn.addStyleName("default");
        checkAllBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                checkAllNodesStatus();
            }
        });
        controlsContent.addComponent(checkAllBtn);

        Button startAllNodesBtn = new Button("Start All");
        startAllNodesBtn.addStyleName("default");
        startAllNodesBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                startAllNodes();
            }
        });
        controlsContent.addComponent(startAllNodesBtn);

        Button stopAllNodesBtn = new Button("Stop All");
        stopAllNodesBtn.addStyleName("default");
        stopAllNodesBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                stopAllNodes();
            }
        });
        controlsContent.addComponent(stopAllNodesBtn);

        Button destroyClusterBtn = new Button("Destroy cluster");
        destroyClusterBtn.addStyleName("default");
        destroyClusterBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                if(config != null) {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format("Do you want to destroy the %s cluster?", config.getClusterName()),
                            "Yes", "No");
                    alert.getOk().addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            UUID trackID = SparkUI.getSparkManager().uninstallCluster(config.getClusterName());
                            ProgressWindow window = new ProgressWindow(SparkUI.getExecutor(), SparkUI.getTracker(),
                                    trackID, SparkClusterConfig.PRODUCT_KEY);
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
                } else
                    show("Please, select cluster");
            }
        });

        controlsContent.addComponent(destroyClusterBtn);

        Button addNodeBtn = new Button("Add Node");
        addNodeBtn.addStyleName("default");
        addNodeBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                if(config != null) {
                    HadoopClusterConfig info = SparkUI.getHadoopManager().getCluster(config.getClusterName());
                    if(info != null) {
                        Set<Agent> nodes = new HashSet<>(info.getAllNodes());
                        nodes.removeAll(config.getSlaveNodes());
                        if(!nodes.isEmpty()) {
                            AddNodeWindow addNodeWindow = new AddNodeWindow(config, nodes);
                            contentRoot.getUI().addWindow(addNodeWindow);
                            addNodeWindow.addCloseListener(new Window.CloseListener() {
                                @Override
                                public void windowClose(Window.CloseEvent closeEvent) {
                                    refreshClustersInfo();
                                }
                            });
                        } else
                            show("All nodes in corresponding Hadoop cluster have Spark installed");
                    } else
                        show("Hadoop cluster info not found");
                } else
                    show("Please, select cluster");
            }
        });

        controlsContent.addComponent(addNodeBtn);

        contentRoot.addComponent(controlsContent, 0, 0);
        contentRoot.addComponent(nodesTable, 0, 1, 0, 9);

    }

    private Table createTableTemplate(String caption) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Start", Button.class, null);
        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Action", Button.class, null);
        table.addContainerProperty("Destroy", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setSizeFull();
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                if(event.isDoubleClick()) {
                    String lxcHostname = (String)table.getItem(event.getItemId()).getItemProperty("Host")
                            .getValue();
                    Agent lxcAgent = SparkUI.getAgentManager().getAgentByHostname(lxcHostname);
                    if(lxcAgent != null) {
                        TerminalWindow terminal = new TerminalWindow(Sets.newHashSet(lxcAgent),
                                SparkUI.getExecutor(), SparkUI.getCommandRunner(), SparkUI.getAgentManager());
                        contentRoot.getUI().addWindow(terminal.getWindow());
                    } else
                        show("Agent is not connected");
                }
            }
        });
        return table;
    }

    private void refreshUI() {
        if(config != null) {
            populateTable(nodesTable, config.getSlaveNodes(), config.getMasterNode());
            checkAllNodesStatus();
        } else
            nodesTable.removeAllItems();
    }

    public void refreshClustersInfo() {
        List<SparkClusterConfig> clustersInfo = SparkUI.getSparkManager().getClusters();
        SparkClusterConfig clusterInfo = (SparkClusterConfig)clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if(clustersInfo != null && clustersInfo.size() > 0) {
            for(SparkClusterConfig mongoClusterInfo : clustersInfo) {
                clusterCombo.addItem(mongoClusterInfo);
                clusterCombo.setItemCaption(mongoClusterInfo,
                        mongoClusterInfo.getClusterName());
            }
            if(clusterInfo != null) {
                for(SparkClusterConfig mongoClusterInfo : clustersInfo) {
                    if(mongoClusterInfo.getClusterName().equals(clusterInfo.getClusterName())) {
                        clusterCombo.setValue(mongoClusterInfo);
                        return;
                    }
                }
            } else
                clusterCombo.setValue(clustersInfo.iterator().next());
        }
    }

    public void checkAllNodesStatus() {
        for(Object o : nodesTable.getItemIds()) {
            int rowId = (Integer)o;
            Item row = nodesTable.getItem(rowId);
            Button checkBtn = (Button)(row.getItemProperty("Check").getValue());
            checkBtn.click();
        }
    }

    public void startAllNodes() {
        for(Object o : nodesTable.getItemIds()) {
            int rowId = (Integer)o;
            Item row = nodesTable.getItem(rowId);
            Button checkBtn = (Button)(row.getItemProperty("Start").getValue());
            checkBtn.click();
        }
    }

    public void stopAllNodes() {
        for(Object o : nodesTable.getItemIds()) {
            int rowId = (Integer)o;
            Item row = nodesTable.getItem(rowId);
            Button checkBtn = (Button)(row.getItemProperty("Stop").getValue());
            checkBtn.click();
        }
    }

    private void show(String notification) {
        Notification.show(notification);
    }

    private void populateTable(final Table table, Set<Agent> agents, final Agent master) {

        table.removeAllItems();

        for(final Agent agent : agents) {
            final Button checkBtn = new Button("Check");
            checkBtn.addStyleName("default");
            final Button startBtn = new Button("Start");
            startBtn.addStyleName("default");
            final Button stopBtn = new Button("Stop");
            stopBtn.addStyleName("default");
            final Button setMasterBtn = new Button("Set As Master");
            setMasterBtn.addStyleName("default");
            final Button destroyBtn = new Button("Destroy");
            destroyBtn.addStyleName("default");
            final Embedded progressIcon = new Embedded("", new ThemeResource("img/spinner.gif"));
            stopBtn.setEnabled(false);
            startBtn.setEnabled(false);
            progressIcon.setVisible(false);

            table.addItem(new Object[]{
                agent.getHostname()
                + String.format(" (%s)", agent.getListIP().get(0)),
                checkBtn,
                startBtn,
                stopBtn,
                master.equals(agent) ? null : setMasterBtn,
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
                    setMasterBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    SparkUI.getExecutor().execute(
                            new CheckTask(config.getClusterName(), agent.getHostname(), false, new CompleteEvent() {

                                @Override
                                public void onComplete(NodeState state) {
                                    synchronized(progressIcon) {
                                        if(state == NodeState.RUNNING)
                                            stopBtn.setEnabled(true);
                                        else if(state == NodeState.STOPPED)
                                            startBtn.setEnabled(true);
                                        setMasterBtn.setEnabled(true);
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
                    setMasterBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    SparkUI.getExecutor().execute(
                            new StartTask(config.getClusterName(), agent.getHostname(), false, new CompleteEvent() {

                                @Override
                                public void onComplete(NodeState state) {
                                    synchronized(progressIcon) {
                                        if(state == NodeState.RUNNING)
                                            stopBtn.setEnabled(true);
                                        else if(state == NodeState.STOPPED)
                                            startBtn.setEnabled(true);
                                        setMasterBtn.setEnabled(true);
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
                    setMasterBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    SparkUI.getExecutor()
                            .execute(new StopTask(config.getClusterName(), agent.getHostname(), false, new CompleteEvent() {

                                @Override
                                public void onComplete(NodeState state) {
                                    synchronized(progressIcon) {
                                        if(state == NodeState.RUNNING)
                                            stopBtn.setEnabled(true);
                                        else if(state == NodeState.STOPPED)
                                            startBtn.setEnabled(true);
                                        setMasterBtn.setEnabled(true);
                                        destroyBtn.setEnabled(true);
                                        progressIcon.setVisible(false);
                                    }
                                }
                            }));
                }
            });

            setMasterBtn.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format("Do you want to set %s as master node?", agent.getHostname()),
                            "Yes", "No");
                    alert.getOk().addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            ConfirmationDialog alert = new ConfirmationDialog(
                                    "Do you want to have a slave on the master node?",
                                    "Yes", "No");
                            alert.getOk().addClickListener(new Button.ClickListener() {
                                @Override
                                public void buttonClick(Button.ClickEvent clickEvent) {
                                    UUID trackID = SparkUI.getSparkManager()
                                            .changeMasterNode(config.getClusterName(), agent.getHostname(), true);
                                    ProgressWindow window = new ProgressWindow(SparkUI.getExecutor(),
                                            SparkUI.getTracker(), trackID, SparkClusterConfig.PRODUCT_KEY);
                                    window.getWindow().addCloseListener(new Window.CloseListener() {
                                        @Override
                                        public void windowClose(Window.CloseEvent closeEvent) {
                                            refreshClustersInfo();
                                        }
                                    });
                                    contentRoot.getUI().addWindow(window.getWindow());
                                }
                            });

                            alert.getCancel().addClickListener(new Button.ClickListener() {
                                @Override
                                public void buttonClick(Button.ClickEvent clickEvent) {
                                    UUID trackID = SparkUI.getSparkManager()
                                            .changeMasterNode(config.getClusterName(), agent.getHostname(), false);
                                    ProgressWindow window = new ProgressWindow(SparkUI.getExecutor(),
                                            SparkUI.getTracker(), trackID, SparkClusterConfig.PRODUCT_KEY);
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

                    contentRoot.getUI().addWindow(alert.getAlert());
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
                            UUID trackID = SparkUI.getSparkManager()
                                    .destroySlaveNode(config.getClusterName(), agent.getHostname());
                            ProgressWindow window = new ProgressWindow(SparkUI.getExecutor(), SparkUI.getTracker(),
                                    trackID, SparkClusterConfig.PRODUCT_KEY);
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

        //add master here
        final Button checkBtn = new Button("Check");
        checkBtn.addStyleName("default");
        final Button startBtn = new Button("Start");
        startBtn.addStyleName("default");
        final Button stopBtn = new Button("Stop");
        stopBtn.addStyleName("default");
        final Embedded progressIcon = new Embedded("", new ThemeResource("img/spinner.gif"));
        stopBtn.setEnabled(false);
        startBtn.setEnabled(false);
        progressIcon.setVisible(false);

        table.addItem(new Object[]{
            MASTER_PREFIX + master.getHostname()
            + String.format(" (%s)", master.getListIP().get(0)),
            checkBtn,
            startBtn,
            stopBtn,
            null,
            null,
            progressIcon},
                null
        );

        checkBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                progressIcon.setVisible(true);
                startBtn.setEnabled(false);
                stopBtn.setEnabled(false);

                SparkUI.getExecutor()
                        .execute(new CheckTask(config.getClusterName(), master.getHostname(), true, new CompleteEvent() {

                            @Override
                            public void onComplete(NodeState state) {
                                synchronized(progressIcon) {
                                    if(state == NodeState.RUNNING)
                                        stopBtn.setEnabled(true);
                                    else if(state == NodeState.STOPPED)
                                        startBtn.setEnabled(true);
                                    progressIcon.setVisible(false);
                                }
                            }
                        }));
            }
        });

        startBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {

                if(!stopBtn.isEnabled())
                    Notification.show("Node already started");

                progressIcon.setVisible(true);
                startBtn.setEnabled(false);
                stopBtn.setEnabled(false);

                SparkUI.getExecutor()
                        .execute(new StartTask(config.getClusterName(), master.getHostname(), true, new CompleteEvent() {

                            @Override
                            public void onComplete(NodeState state) {
                                synchronized(progressIcon) {
                                    if(state == NodeState.RUNNING)
                                        stopBtn.setEnabled(true);
                                    else if(state == NodeState.STOPPED)
                                        startBtn.setEnabled(true);
                                    progressIcon.setVisible(false);
                                }
                            }
                        }));
            }
        });

        stopBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {

                if(!startBtn.isEnabled())
                    Notification.show("Node already stopped");

                progressIcon.setVisible(true);
                startBtn.setEnabled(false);
                stopBtn.setEnabled(false);

                SparkUI.getExecutor()
                        .execute(new StopTask(config.getClusterName(), master.getHostname(), true, new CompleteEvent() {

                            @Override
                            public void onComplete(NodeState state) {
                                synchronized(progressIcon) {
                                    if(state == NodeState.RUNNING)
                                        stopBtn.setEnabled(true);
                                    else if(state == NodeState.STOPPED)
                                        startBtn.setEnabled(true);
                                    progressIcon.setVisible(false);
                                }
                            }
                        }));
            }
        });
    }

    public Component getContent() {
        return contentRoot;
    }

}
