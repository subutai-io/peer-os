package org.safehaus.subutai.plugin.storm.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.storm.ui.manager.Manager;
import org.safehaus.subutai.plugin.storm.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class StormComponent extends CustomComponent
{

    private final Wizard wizard;
    private final Manager manager;


    public StormComponent( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        manager = new Manager( executorService, serviceLocator );
        wizard = new Wizard( executorService, serviceLocator );

        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab( wizard.getContent(), "Install" );
        tabSheet.getTab( 0 ).setId( "StormInstallTab" );
        tabSheet.addTab( manager.getContent(), "Manage" );
        tabSheet.getTab( 1 ).setId( "StormManageTab" );

        verticalLayout.addComponent( tabSheet );
        setCompositionRoot( verticalLayout );

        manager.refreshClustersInfo();
    }
}
