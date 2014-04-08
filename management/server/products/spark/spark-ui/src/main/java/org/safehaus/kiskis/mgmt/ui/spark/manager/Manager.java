/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.spark.manager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.kiskis.mgmt.api.spark.Config;
import org.safehaus.kiskis.mgmt.server.ui.ConfirmationDialogCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CompleteEvent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.spark.SparkUI;

/**
 * @author dilshat
 */
public class Manager {

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private Config config;
    private final String MASTER_PREFIX = "Master: ";

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

        controlsContent.addComponent(clusterCombo);

        Button refreshClustersBtn = new Button("Refresh clusters");
        refreshClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                refreshClustersInfo();
            }
        });
        controlsContent.addComponent(refreshClustersBtn);

        Button checkAllBtn = new Button("Check All");

        checkAllBtn.addListener(new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                for (Iterator it = nodesTable.getItemIds().iterator(); it.hasNext(); ) {
                    int rowId = (Integer) it.next();
                    Item row = nodesTable.getItem(rowId);
                    Button checkBtn = (Button) (row.getItemProperty("Check").getValue());
                    checkBtn.click();
                }
            }
        });
        controlsContent.addComponent(checkAllBtn);

        Button destroyClusterBtn = new Button("Destroy cluster");
        destroyClusterBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config != null) {
                    MgmtApplication.showConfirmationDialog(
                            "Cluster destruction confirmation",
                            String.format("Do you want to destroy the %s cluster?", config.getClusterName()),
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {
                                        UUID trackID = SparkUI.getSparkManager().uninstallCluster(config.getClusterName());
                                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                                            public void windowClose(Window.CloseEvent e) {
                                                refreshClustersInfo();
                                            }
                                        });
                                    }
                                }
                            }
                    );
                } else {
                    show("Please, select cluster");
                }
            }

        });

        controlsContent.addComponent(destroyClusterBtn);

        Button addNodeBtn = new Button("Add Node");

        addNodeBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config != null) {
                    HadoopClusterInfo info = SparkUI.getDbManager().
                            getInfo(HadoopClusterInfo.SOURCE,
                                    config.getClusterName(),
                                    HadoopClusterInfo.class);
                    if (info != null) {
                        Set<Agent> nodes = new HashSet<Agent>(info.getAllAgents());
                        nodes.removeAll(config.getSlaveNodes());
                        if (!nodes.isEmpty()) {
                            AddNodeWindow addNodeWindow = new AddNodeWindow(config, nodes);
                            MgmtApplication.addCustomWindow(addNodeWindow);
                            addNodeWindow.addListener(new Window.CloseListener() {

                                public void windowClose(Window.CloseEvent e) {
                                    refreshClustersInfo();
                                }
                            });
                        } else {
                            show("All nodes in corresponding Hadoop cluster have Spark installed");
                        }
                    } else {
                        show("Hadoop cluster info not found");
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

    public Component getContent() {
        return contentRoot;
    }

    private void show(String notification) {
        contentRoot.getWindow().showNotification(notification);
    }

    /*
     * @todo separate master from slaves
     */
    private void populateTable(final Table table, Set<Agent> agents, final Agent master) {

        table.removeAllItems();

        for (Iterator it = agents.iterator(); it.hasNext(); ) {
            final Agent agent = (Agent) it.next();

            final Button checkBtn = new Button("Check");
            final Button startBtn = new Button("Start");
            final Button stopBtn = new Button("Stop");
            final Button setMasterBtn = new Button("Set As Master");
            final Button destroyBtn = new Button("Destroy");
            final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
            stopBtn.setEnabled(false);
            startBtn.setEnabled(false);
            progressIcon.setVisible(false);

            final Object rowId = table.addItem(new Object[]{
                            agent.getHostname(),
                            checkBtn,
                            startBtn,
                            stopBtn,
                            setMasterBtn,
                            destroyBtn,
                            progressIcon},
                    null
            );

            checkBtn.addListener(new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    progressIcon.setVisible(true);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    setMasterBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    SparkUI.getExecutor().execute(new CheckTask(config.getClusterName(), agent.getHostname(), false, new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
                                if (state == NodeState.RUNNING) {
                                    stopBtn.setEnabled(true);
                                } else if (state == NodeState.STOPPED) {
                                    startBtn.setEnabled(true);
                                }
                                setMasterBtn.setEnabled(true);
                                destroyBtn.setEnabled(true);
                                progressIcon.setVisible(false);
                            }
                        }
                    }));
                }
            });

            startBtn.addListener(new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    progressIcon.setVisible(true);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    setMasterBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    SparkUI.getExecutor().execute(new StartTask(config.getClusterName(), agent.getHostname(), false, new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
                                if (state == NodeState.RUNNING) {
                                    stopBtn.setEnabled(true);
                                } else if (state == NodeState.STOPPED) {
                                    startBtn.setEnabled(true);
                                }
                                setMasterBtn.setEnabled(true);
                                destroyBtn.setEnabled(true);
                                progressIcon.setVisible(false);
                            }
                        }
                    }));
                }
            });

            stopBtn.addListener(new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    progressIcon.setVisible(true);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    setMasterBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    SparkUI.getExecutor().execute(new StopTask(config.getClusterName(), agent.getHostname(), false, new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
                                if (state == NodeState.RUNNING) {
                                    stopBtn.setEnabled(true);
                                } else if (state == NodeState.STOPPED) {
                                    startBtn.setEnabled(true);
                                }
                                setMasterBtn.setEnabled(true);
                                destroyBtn.setEnabled(true);
                                progressIcon.setVisible(false);
                            }
                        }
                    }));
                }
            });

            setMasterBtn.addListener(new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    MgmtApplication.showConfirmationDialog(
                            "Master change confirmation",
                            String.format("Do you want to set %s as master node?", agent.getHostname()),
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {

                                        MgmtApplication.showConfirmationDialog(
                                                "Setup slave confirmation",
                                                "Do you want to have a slave on the master node?",
                                                "Yes", "No", new ConfirmationDialogCallback() {

                                                    @Override
                                                    public void response(boolean ok) {

                                                        UUID trackID = SparkUI.getSparkManager().changeMasterNode(config.getClusterName(), agent.getHostname(), ok);
                                                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                                                            public void windowClose(Window.CloseEvent e) {
                                                                refreshClustersInfo();
                                                            }
                                                        });

                                                    }
                                                }
                                        );
                                    }
                                }
                            }
                    );
                }
            });

            destroyBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    MgmtApplication.showConfirmationDialog(
                            "Node destruction confirmation",
                            String.format("Do you want to destroy the %s node?", agent.getHostname()),
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {
                                        UUID trackID = SparkUI.getSparkManager().destroySlaveNode(config.getClusterName(), agent.getHostname());
                                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                                            public void windowClose(Window.CloseEvent e) {
                                                refreshClustersInfo();
                                            }
                                        });
                                    }
                                }
                            }
                    );

                }
            });
        }

        //add master here
        final Button checkBtn = new Button("Check");
        final Button startBtn = new Button("Start");
        final Button stopBtn = new Button("Stop");
        final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
        stopBtn.setEnabled(false);
        startBtn.setEnabled(false);
        progressIcon.setVisible(false);

        final Object rowId = table.addItem(new Object[]{
                        MASTER_PREFIX + master.getHostname(),
                        checkBtn,
                        startBtn,
                        stopBtn,
                        null,
                        null,
                        progressIcon},
                null
        );

        checkBtn.addListener(new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                progressIcon.setVisible(true);
                startBtn.setEnabled(false);
                stopBtn.setEnabled(false);

                SparkUI.getExecutor().execute(new CheckTask(config.getClusterName(), master.getHostname(), true, new CompleteEvent() {

                    public void onComplete(NodeState state) {
                        synchronized (progressIcon) {
                            if (state == NodeState.RUNNING) {
                                stopBtn.setEnabled(true);
                            } else if (state == NodeState.STOPPED) {
                                startBtn.setEnabled(true);
                            }
                            progressIcon.setVisible(false);
                        }
                    }
                }));
            }
        });

        startBtn.addListener(new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                progressIcon.setVisible(true);
                startBtn.setEnabled(false);
                stopBtn.setEnabled(false);

                SparkUI.getExecutor().execute(new StartTask(config.getClusterName(), master.getHostname(), true, new CompleteEvent() {

                    public void onComplete(NodeState state) {
                        synchronized (progressIcon) {
                            if (state == NodeState.RUNNING) {
                                stopBtn.setEnabled(true);
                            } else if (state == NodeState.STOPPED) {
                                startBtn.setEnabled(true);
                            }
                            progressIcon.setVisible(false);
                        }
                    }
                }));
            }
        });

        stopBtn.addListener(new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                progressIcon.setVisible(true);
                startBtn.setEnabled(false);
                stopBtn.setEnabled(false);

                SparkUI.getExecutor().execute(new StopTask(config.getClusterName(), master.getHostname(), true, new CompleteEvent() {

                    public void onComplete(NodeState state) {
                        synchronized (progressIcon) {
                            if (state == NodeState.RUNNING) {
                                stopBtn.setEnabled(true);
                            } else if (state == NodeState.STOPPED) {
                                startBtn.setEnabled(true);
                            }
                            progressIcon.setVisible(false);
                        }
                    }
                }));
            }
        });

    }

    private void refreshUI() {
        if (config != null) {
            populateTable(nodesTable, config.getSlaveNodes(), config.getMasterNode());
        } else {
            nodesTable.removeAllItems();
        }
    }

    public void refreshClustersInfo() {
        List<Config> clustersInfo = SparkUI.getSparkManager().getClusters();
        Config clusterInfo = (Config) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if (clustersInfo != null && clustersInfo.size() > 0) {
            for (Config mongoClusterInfo : clustersInfo) {
                clusterCombo.addItem(mongoClusterInfo);
                clusterCombo.setItemCaption(mongoClusterInfo,
                        mongoClusterInfo.getClusterName());
            }
            if (clusterInfo != null) {
                for (Config mongoClusterInfo : clustersInfo) {
                    if (mongoClusterInfo.getClusterName().equals(clusterInfo.getClusterName())) {
                        clusterCombo.setValue(mongoClusterInfo);
                        return;
                    }
                }
            } else {
                clusterCombo.setValue(clustersInfo.iterator().next());
            }
        }
    }

    private Table createTableTemplate(String caption, int height) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Start", Button.class, null);
        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Action", Button.class, null);
        table.addContainerProperty("Destroy", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(height, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    lxcHostname = lxcHostname.replaceAll(MASTER_PREFIX, "");
                    Agent lxcAgent = SparkUI.getAgentManager().getAgentByHostname(lxcHostname);
                    if (lxcAgent != null) {
                        Window terminal = MgmtApplication.createTerminalWindow(Util.wrapAgentToSet(lxcAgent));
                        MgmtApplication.addCustomWindow(terminal);
                    } else {
                        show("Agent is not connected");
                    }
                }
            }
        });
        return table;
    }

}
