package org.safehaus.subutai.plugin.storm.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.storm.ui.manager.Manager;
import org.safehaus.subutai.plugin.storm.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class StormForm extends CustomComponent {

    private final Wizard wizard;
    private final Manager manager;


    public StormForm( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException {
        manager = new Manager( executorService, serviceLocator );
        wizard = new Wizard( executorService, serviceLocator );

        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab( wizard.getContent(), "Install" );
        tabSheet.addTab( manager.getContent(), "Manage" );

        verticalLayout.addComponent( tabSheet );
        setCompositionRoot( verticalLayout );

        manager.refreshClustersInfo();
    }
}
