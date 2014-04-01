/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.lxcmanager;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.server.ui.MgmtAgentManager;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Disposable;
import org.safehaus.kiskis.mgmt.ui.lxcmanager.clone.Cloner;
import org.safehaus.kiskis.mgmt.ui.lxcmanager.manage.Manager;

/**
 *
 * @author dilshat
 */
public class LxcForm extends CustomComponent implements Disposable {

    private final MgmtAgentManager agentTree;
    private final static String managerTabCaption = "Manage";

    public LxcForm(AgentManager agentManager, LxcManager lxcManager) {
        setHeight(100, UNITS_PERCENTAGE);

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);
        horizontalSplit.setSplitPosition(200, UNITS_PIXELS);
        agentTree = MgmtApplication.createAgentTree();
        horizontalSplit.setFirstComponent(agentTree);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();
        TabSheet commandsSheet = new TabSheet();
        commandsSheet.setStyleName(Runo.TABSHEET_SMALL);
        commandsSheet.setSizeFull();
        final Manager manager = new Manager(agentManager, lxcManager);
        commandsSheet.addTab(new Cloner(lxcManager, agentTree), "Clone");
        commandsSheet.addTab(manager, managerTabCaption);
        commandsSheet.addListener(new TabSheet.SelectedTabChangeListener() {
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab(event.getTabSheet().getSelectedTab()).getCaption();
                if (caption.equals(managerTabCaption)) {
                    manager.getLxcInfo();
                }
            }
        });
        verticalLayout.addComponent(commandsSheet);

        horizontalSplit.setSecondComponent(verticalLayout);
        setCompositionRoot(horizontalSplit);
    }

    public void dispose() {
        agentTree.dispose();
    }

}
