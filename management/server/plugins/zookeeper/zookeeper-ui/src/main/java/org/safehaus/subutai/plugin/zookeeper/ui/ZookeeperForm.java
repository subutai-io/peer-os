/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.ui;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.plugin.zookeeper.ui.manager.Manager;
import org.safehaus.subutai.plugin.zookeeper.ui.wizard.Wizard;

/**
 * @author dilshat
 */
public class ZookeeperForm extends CustomComponent {

	public ZookeeperForm() {
		setSizeFull();

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();

		TabSheet zookeeperSheet = new TabSheet();
		zookeeperSheet.setSizeFull();
		final Manager manager = new Manager();
		Wizard wizard = new Wizard();
		zookeeperSheet.addTab( wizard.getContent(), "Install" );
		zookeeperSheet.addTab( manager.getContent(), "Manage" );
        zookeeperSheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange( TabSheet.SelectedTabChangeEvent event ) {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab( event.getTabSheet().getSelectedTab() ).getCaption();
                if( caption.equals( "Manage" ) ) {
                    manager.refreshClustersInfo();
                }
            }
        } );
		verticalLayout.addComponent(zookeeperSheet);

		setCompositionRoot(verticalLayout);
		manager.refreshClustersInfo();
	}

}
