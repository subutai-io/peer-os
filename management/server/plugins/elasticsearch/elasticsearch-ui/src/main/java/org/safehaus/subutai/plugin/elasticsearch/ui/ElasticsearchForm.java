package org.safehaus.subutai.plugin.elasticsearch.ui;

import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.elasticsearch.ui.manager.*;
import org.safehaus.subutai.plugin.elasticsearch.ui.wizard.*;

public class ElasticsearchForm extends CustomComponent {

	private final Wizard wizard;
	private final Manager manager;

	public ElasticsearchForm(ExecutorService executorService, ServiceLocator serviceLocator) throws NamingException{
		setSizeFull();

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();

		setCompositionRoot(verticalLayout);

		TabSheet sheet = new TabSheet();
		sheet.setSizeFull();
		manager = new Manager(executorService, serviceLocator);
		wizard = new Wizard(executorService, serviceLocator);
		sheet.addTab(wizard.getContent(), "Install");
		sheet.addTab(manager.getContent(), "Manage");
        sheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange( TabSheet.SelectedTabChangeEvent event ) {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab( event.getTabSheet().getSelectedTab() ).getCaption();
                if ( caption.equals( "Manage" ) ) {
                    manager.refreshClustersInfo();
                }
            }
        } );
        verticalLayout.addComponent( sheet );
        verticalLayout.addComponent( sheet );
		verticalLayout.addComponent( sheet );

		manager.refreshClustersInfo();
	}
}
