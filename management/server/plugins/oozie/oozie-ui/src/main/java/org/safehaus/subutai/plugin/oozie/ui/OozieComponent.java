package org.safehaus.subutai.plugin.oozie.ui;


import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.oozie.ui.manager.Manager;
import org.safehaus.subutai.plugin.oozie.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import javax.naming.NamingException;
import java.util.concurrent.ExecutorService;


public class OozieComponent extends CustomComponent
{

    private final Wizard wizard;
    private final Manager manager;


    public OozieComponent( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException {


        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet mongoSheet = new TabSheet();
        mongoSheet.setSizeFull();
        manager = new Manager( executorService, serviceLocator );
        wizard = new Wizard( executorService, serviceLocator );
        mongoSheet.addTab( wizard.getContent(), "Install" );
        mongoSheet.addTab( manager.getContent(), "Manage" );
        verticalLayout.addComponent( mongoSheet );

        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}
