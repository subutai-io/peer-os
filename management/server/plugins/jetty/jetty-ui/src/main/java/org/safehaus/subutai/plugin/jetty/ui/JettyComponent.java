package org.safehaus.subutai.plugin.jetty.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.jetty.ui.manager.Manager;
import org.safehaus.subutai.plugin.jetty.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class JettyComponent extends CustomComponent
{
    private final Wizard wizard;
    private final Manager manager;


    public JettyComponent( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        setCompositionRoot( verticalLayout );

        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();

        manager = new Manager( executorService, serviceLocator );
        wizard = new Wizard( executorService, serviceLocator );
        sheet.addTab( wizard.getContent(), "Install" );
        sheet.getTab(0).setId("JettyInstallTab");
        sheet.addTab( manager.getContent(), "Manage" );
        sheet.getTab(1).setId("JettyManageTab");
        sheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener()
        {
            @Override
            public void selectedTabChange( TabSheet.SelectedTabChangeEvent event )
            {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab( event.getTabSheet().getSelectedTab() ).getCaption();
                if ( caption.equals( "Manage" ) )
                {
                    manager.refreshClustersInfo();
                }
            }
        } );
        verticalLayout.addComponent( sheet );
        manager.refreshClustersInfo();
    }
}
