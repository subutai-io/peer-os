package org.safehaus.subutai.plugin.flume.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.flume.ui.manager.Manager;
import org.safehaus.subutai.plugin.flume.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class FlumeComponent extends CustomComponent
{

    private final Wizard wizard;
    private final Manager manager;


    public FlumeComponent( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        manager = new Manager( executorService, serviceLocator );
        wizard = new Wizard( executorService, serviceLocator );
        tabSheet.addTab( wizard.getContent(), "Install" );
        tabSheet.addTab( manager.getContent(), "Manage" );

        verticalLayout.addComponent( tabSheet );
        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}
