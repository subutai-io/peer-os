package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.manage;

import com.vaadin.data.Item;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.safehaus.kiskis.mgmt.server.ui.ConfirmationDialogCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.Buttons;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcState;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.LxcModule;

@SuppressWarnings("serial")
public class Manager extends VerticalLayout {

    private final Label indicator;
    private final Button infoBtn;
    private final Button startAllBtn;
    private final Button stopAllBtn;
    private final Button destroyAllBtn;
    private final TreeTable lxcTable;
    private final LxcManager lxcManager;
    private final AgentManager agentManager;
    private final static String physicalHostLabel = "Physical Host";
    private volatile boolean isDestroyAllButtonClicked = false;

    public Manager(AgentManager agentManager, LxcManager lxcManager) {

        setSpacing(true);
        setMargin(true);

        this.agentManager = agentManager;
        this.lxcManager = lxcManager;

        lxcTable = createTableTemplate("Lxc containers", 500);

        infoBtn = new Button(Buttons.INFO.getButtonLabel());
        infoBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getLxcInfo();
            }
        });

        stopAllBtn = new Button(Buttons.STOP_ALL.getButtonLabel());
        stopAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (Iterator it = lxcTable.getItemIds().iterator(); it.hasNext();) {
                    Item row = lxcTable.getItem(it.next());
                    Button stopBtn = (Button) (row.getItemProperty(Buttons.STOP.getButtonLabel()).getValue());
                    if (stopBtn != null) {
                        stopBtn.click();
                    }
                }
            }
        });
        startAllBtn = new Button(Buttons.START_ALL.getButtonLabel());
        startAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (Iterator it = lxcTable.getItemIds().iterator(); it.hasNext();) {
                    Item row = lxcTable.getItem(it.next());
                    Button startBtn = (Button) (row.getItemProperty(Buttons.START.getButtonLabel()).getValue());
                    if (startBtn != null) {
                        startBtn.click();
                    }
                }
            }
        });
        destroyAllBtn = new Button(Buttons.DESTROY_ALL.getButtonLabel());
        destroyAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                MgmtApplication.showConfirmationDialog(
                        "Lxc destruction confirmation",
                        "Do you want to destroy all lxc nodes?",
                        "Yes", "No", new ConfirmationDialogCallback() {

                            @Override
                            public void response(boolean ok) {
                                if (ok) {
                                    isDestroyAllButtonClicked = true;
                                    for (Iterator it = lxcTable.getItemIds().iterator(); it.hasNext();) {
                                        Item row = lxcTable.getItem(it.next());
                                        Button destroyBtn = (Button) (row.getItemProperty(Buttons.DESTROY.getButtonLabel()).getValue());
                                        if (destroyBtn != null && row.getItemProperty(physicalHostLabel).getValue() == null) {
                                            destroyBtn.click();
                                        }
                                    }
                                    isDestroyAllButtonClicked = false;
                                }
                            }
                        });
            }
        });

        indicator = new Label();
        indicator.setIcon(new ThemeResource("icons/indicator.gif"));
        indicator.setContentMode(Label.CONTENT_XHTML);
        indicator.setHeight(11, Sizeable.UNITS_PIXELS);
        indicator.setWidth(50, Sizeable.UNITS_PIXELS);
        indicator.setVisible(false);

        GridLayout grid = new GridLayout(5, 1);
        grid.setSpacing(true);

        grid.addComponent(infoBtn);
        grid.addComponent(startAllBtn);
        grid.addComponent(stopAllBtn);
        grid.addComponent(destroyAllBtn);
        grid.addComponent(indicator);
        grid.setComponentAlignment(indicator, Alignment.MIDDLE_CENTER);
        addComponent(grid);

        addComponent(lxcTable);

    }

    private TreeTable createTableTemplate(String caption, int size) {
        TreeTable table = new TreeTable(caption);
        table.addContainerProperty(physicalHostLabel, String.class, null);
        table.addContainerProperty("Lxc Host", String.class, null);
        table.addContainerProperty(Buttons.START.getButtonLabel(), Button.class, null);
        table.addContainerProperty(Buttons.STOP.getButtonLabel(), Button.class, null);
        table.addContainerProperty(Buttons.DESTROY.getButtonLabel(), Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);
        return table;
    }

    public void getLxcInfo() {
        lxcTable.setEnabled(false);
        indicator.setVisible(true);
        LxcModule.getExecutor().execute(new Runnable() {

            public void run() {
                Map<String, EnumMap<LxcState, List<String>>> agentFamilies = lxcManager.getLxcOnPhysicalServers();
                populateTable(agentFamilies);
                clearEmptyParents();
                lxcTable.setEnabled(true);
                indicator.setVisible(false);

            }
        });

    }

    private void clearEmptyParents() {
        //clear empty parents
        for (Iterator it = lxcTable.getItemIds().iterator(); it.hasNext();) {
            Object rowId = it.next();
            Item row = lxcTable.getItem(rowId);
            if (row != null && row.getItemProperty(physicalHostLabel).getValue() != null
                    && (lxcTable.getChildren(rowId) == null || lxcTable.getChildren(rowId).isEmpty())) {
                lxcTable.removeItem(rowId);
            }
        }
    }

    private void populateTable(Map<String, EnumMap<LxcState, List<String>>> agentFamilies) {
        lxcTable.removeAllItems();

        for (Map.Entry<String, EnumMap<LxcState, List<String>>> agentFamily : agentFamilies.entrySet()) {
            final String parentHostname = agentFamily.getKey();
            final Button startAllChildrenBtn = new Button(Buttons.START.getButtonLabel());
            final Button stopAllChildrenBtn = new Button(Buttons.STOP.getButtonLabel());
            final Button destroyAllChildrenBtn = new Button(Buttons.DESTROY.getButtonLabel());
            final Object parentId = lxcTable.addItem(new Object[]{parentHostname, null, startAllChildrenBtn, stopAllChildrenBtn, destroyAllChildrenBtn, null}, parentHostname);
            lxcTable.setCollapsed(parentHostname, false);

            startAllChildrenBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Collection col = lxcTable.getChildren(parentId);
                    if (col != null) {
                        for (Iterator it = col.iterator(); it.hasNext();) {
                            Item row = lxcTable.getItem(it.next());
                            Button startBtn = (Button) (row.getItemProperty(Buttons.START.getButtonLabel()).getValue());
                            if (startBtn != null) {
                                startBtn.click();
                            }
                        }
                    }
                }
            });

            stopAllChildrenBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Collection col = lxcTable.getChildren(parentId);
                    if (col != null) {
                        for (Iterator it = col.iterator(); it.hasNext();) {
                            Item row = lxcTable.getItem(it.next());
                            Button stopBtn = (Button) (row.getItemProperty(Buttons.STOP.getButtonLabel()).getValue());
                            if (stopBtn != null) {
                                stopBtn.click();
                            }
                        }
                    }
                }
            });

            destroyAllChildrenBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    MgmtApplication.showConfirmationDialog(
                            "Lxc destruction confirmation",
                            "Do you want to destroy all lxc nodes on this physical node?",
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {
                                        Collection col = lxcTable.getChildren(parentId);
                                        if (col != null) {
                                            isDestroyAllButtonClicked = true;
                                            for (Iterator it = col.iterator(); it.hasNext();) {
                                                Item row = lxcTable.getItem(it.next());
                                                Button destroyBtn = (Button) (row.getItemProperty(Buttons.DESTROY.getButtonLabel()).getValue());
                                                if (destroyBtn != null) {
                                                    destroyBtn.click();
                                                }
                                            }
                                            isDestroyAllButtonClicked = false;
                                        }
                                    }
                                }
                            });
                }
            });

            for (Map.Entry<LxcState, List<String>> lxcs : agentFamily.getValue().entrySet()) {

                for (final String lxcHostname : lxcs.getValue()) {
                    final Button startBtn = new Button(Buttons.START.getButtonLabel());
                    final Button stopBtn = new Button(Buttons.STOP.getButtonLabel());
                    final Button destroyBtn = new Button(Buttons.DESTROY.getButtonLabel());
                    final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
                    progressIcon.setVisible(false);

                    if (lxcs.getKey() == LxcState.RUNNING) {
                        startBtn.setEnabled(false);
                    } else if (lxcs.getKey() == LxcState.STOPPED) {
                        stopBtn.setEnabled(false);
                    }
                    final Object rowId = lxcTable.addItem(new Object[]{
                        null,
                        lxcHostname,
                        startBtn,
                        stopBtn,
                        destroyBtn,
                        progressIcon
                    },
                            lxcHostname);

                    lxcTable.setParent(lxcHostname, parentHostname);
                    lxcTable.setChildrenAllowed(lxcHostname, false);

                    startBtn.addListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {

                            final Agent physicalAgent = agentManager.getAgentByHostname(parentHostname);
                            if (physicalAgent != null) {
                                startBtn.setEnabled(false);
                                destroyBtn.setEnabled(false);
                                progressIcon.setVisible(true);
                                LxcModule.getExecutor().execute(new Runnable() {

                                    public void run() {
                                        boolean success = lxcManager.startLxcOnHost(physicalAgent, lxcHostname);
                                        if (success) {
                                            stopBtn.setEnabled(true);
                                        } else {
                                            startBtn.setEnabled(true);
                                        }
                                        destroyBtn.setEnabled(true);
                                        progressIcon.setVisible(false);
                                    }
                                });

                            }
                        }
                    });
                    stopBtn.addListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {

                            final Agent physicalAgent = agentManager.getAgentByHostname(parentHostname);
                            if (physicalAgent != null) {
                                stopBtn.setEnabled(false);
                                destroyBtn.setEnabled(false);
                                progressIcon.setVisible(true);
                                LxcModule.getExecutor().execute(new Runnable() {

                                    public void run() {
                                        boolean success = lxcManager.stopLxcOnHost(physicalAgent, lxcHostname);
                                        if (!success) {
                                            stopBtn.setEnabled(true);
                                        } else {
                                            startBtn.setEnabled(true);
                                        }
                                        destroyBtn.setEnabled(true);
                                        progressIcon.setVisible(false);
                                    }
                                });

                            }
                        }
                    });
                    destroyBtn.addListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            if (!isDestroyAllButtonClicked) {
                                MgmtApplication.showConfirmationDialog(
                                        "Lxc destruction confirmation",
                                        "Do you want to destroy this lxc node?",
                                        "Yes", "No", new ConfirmationDialogCallback() {

                                            @Override
                                            public void response(boolean ok) {
                                                if (ok) {
                                                    final Agent physicalAgent = agentManager.getAgentByHostname(parentHostname);
                                                    if (physicalAgent != null) {
                                                        startBtn.setEnabled(false);
                                                        stopBtn.setEnabled(false);
                                                        destroyBtn.setEnabled(false);
                                                        progressIcon.setVisible(true);
                                                        LxcModule.getExecutor().execute(new Runnable() {

                                                            public void run() {
                                                                boolean success = lxcManager.destroyLxcOnHost(physicalAgent, lxcHostname);
                                                                if (!success) {
                                                                    stopBtn.setEnabled(true);
                                                                    destroyBtn.setEnabled(true);
                                                                    progressIcon.setVisible(false);
                                                                } else {
                                                                    //remove row
                                                                    lxcTable.removeItem(rowId);
                                                                    clearEmptyParents();
                                                                }
                                                            }
                                                        });

                                                    }
                                                }
                                            }
                                        });
                            } else {

                                final Agent physicalAgent = agentManager.getAgentByHostname(parentHostname);
                                if (physicalAgent != null) {
                                    startBtn.setEnabled(false);
                                    stopBtn.setEnabled(false);
                                    destroyBtn.setEnabled(false);
                                    progressIcon.setVisible(true);
                                    LxcModule.getExecutor().execute(new Runnable() {

                                        public void run() {
                                            boolean success = lxcManager.destroyLxcOnHost(physicalAgent, lxcHostname);
                                            if (!success) {
                                                stopBtn.setEnabled(true);
                                                destroyBtn.setEnabled(true);
                                                progressIcon.setVisible(false);
                                            } else {
                                                //remove row
                                                lxcTable.removeItem(rowId);
                                                clearEmptyParents();
                                            }
                                        }
                                    });

                                }
                            }

                        }
                    });

                }
            }
        }

    }

}
