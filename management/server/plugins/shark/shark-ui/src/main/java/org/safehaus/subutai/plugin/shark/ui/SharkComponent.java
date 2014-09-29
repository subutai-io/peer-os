package org.safehaus.subutai.plugin.shark.ui;


import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import java.util.concurrent.ExecutorService;
import javax.naming.NamingException;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.shark.ui.manager.Manager;
import org.safehaus.subutai.plugin.shark.ui.wizard.Wizard;


public class SharkComponent extends CustomComponent
{

    public SharkComponent( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        Manager manager = new Manager( executorService, serviceLocator );
        Wizard wizard = new Wizard( executorService, serviceLocator );
        tabSheet.addTab( wizard.getContent(), "Install" );
        tabSheet.addTab( manager.getContent(), "Manage" );
        verticalLayout.addComponent( tabSheet );

        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }


}

