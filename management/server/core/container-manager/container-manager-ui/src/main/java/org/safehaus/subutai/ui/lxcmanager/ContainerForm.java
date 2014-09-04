/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.lxcmanager;


import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.server.ui.component.AgentTree;
import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.ui.lxcmanager.clone.Cloner;
import org.safehaus.subutai.ui.lxcmanager.manage.Manager;

/**
 *
 */
public class ContainerForm extends CustomComponent implements Disposable {

	private final static String managerTabCaption = "Manage";
	private final AgentTree agentTree;


	public ContainerForm(AgentManager agentManager, LxcManager lxcManager) {
		setHeight(100, Unit.PERCENTAGE);

		HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
		horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);
		horizontalSplit.setSplitPosition(200, Unit.PIXELS);
		agentTree = new AgentTree(agentManager);
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
		commandsSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
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
