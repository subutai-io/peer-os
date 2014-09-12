package org.safehaus.subutai.plugin.presto.ui;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.plugin.presto.ui.manager.Manager;
import org.safehaus.subutai.plugin.presto.ui.wizard.Wizard;

public class PrestoForm extends CustomComponent {

    public PrestoForm() {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();

        TabSheet cassandraSheet = new TabSheet();
        cassandraSheet.setSizeFull();
        final Manager manager = new Manager();
        Wizard wizard = new Wizard();
        cassandraSheet.addTab( wizard.getContent(), "Install" );
        cassandraSheet.addTab( manager.getContent(), "Manage" );
        cassandraSheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange( TabSheet.SelectedTabChangeEvent event ) {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab( event.getTabSheet().getSelectedTab() ).getCaption();
                if ( caption.equals( "Manage" ) ) {
                    manager.refreshClustersInfo();
                }
            }
        } );
        verticalLayout.addComponent( cassandraSheet );
        verticalLayout.addComponent(cassandraSheet);

        setCompositionRoot(verticalLayout);
        manager.refreshClustersInfo();
    }

}
