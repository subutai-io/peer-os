package org.safehaus.subutai.plugin.sqoop.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.sqoop.ui.manager.ExportPanel;
import org.safehaus.subutai.plugin.sqoop.ui.manager.ImportExportBase;
import org.safehaus.subutai.plugin.sqoop.ui.manager.ImportPanel;
import org.safehaus.subutai.plugin.sqoop.ui.manager.Manager;
import org.safehaus.subutai.plugin.sqoop.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class SqoopForm extends CustomComponent {

    private final Wizard wizard;
    private final Manager manager;

    private final TabSheet tabSheet;


    public SqoopForm( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException {
        manager = new Manager( executorService, serviceLocator, this );
        wizard = new Wizard( executorService, serviceLocator );

        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab( wizard.getContent(), "Install" );
        tabSheet.addTab( manager.getContent(), "Manage" );

        verticalLayout.addComponent( tabSheet );
        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }


    public void addTab( ImportExportBase component ) {
        TabSheet.Tab tab = tabSheet.addTab( component );
        if ( component instanceof ExportPanel ) {
            tab.setCaption( "Export" );
        }
        else if ( component instanceof ImportPanel ) {
            tab.setCaption( "Import" );
        }
        tabSheet.setSelectedTab( component );
    }
}
