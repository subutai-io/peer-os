/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.accumulo;


import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.ui.accumulo.manager.Manager;
import org.safehaus.subutai.ui.accumulo.wizard.Wizard;


/**
 * @author dilshat
 */
public class AccumuloForm extends CustomComponent {

	public AccumuloForm() {

		setSizeFull();

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();

		TabSheet accumuloSheet = new TabSheet();
		accumuloSheet.setSizeFull();

		final Manager manager = new Manager();
		Wizard wizard = new Wizard();
		accumuloSheet.addTab( wizard.getContent(), "Install" );
		accumuloSheet.addTab( manager.getContent(), "Manage" );
        accumuloSheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange( TabSheet.SelectedTabChangeEvent event ) {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab( event.getTabSheet().getSelectedTab() ).getCaption();
                if( caption.equals( "Manage" ) ) {
                    manager.refreshClustersInfo();
                }
            }
        } );
		verticalLayout.addComponent(accumuloSheet);
		setCompositionRoot(verticalLayout);
		manager.refreshClustersInfo();
	}
}
