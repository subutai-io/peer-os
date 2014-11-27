package org.safehaus.subutai.plugin.shark.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.ui.manager.Manager;
import org.safehaus.subutai.plugin.shark.ui.wizard.Wizard;
import org.safehaus.subutai.plugin.spark.api.Spark;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class SharkComponent extends CustomComponent
{

    public SharkComponent( ExecutorService executorService, Shark shark, Spark spark, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {
        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();

        final Manager manager = new Manager( executorService, shark, spark, tracker, environmentManager );
        Wizard wizard = new Wizard( executorService, shark, spark, tracker );
        sheet.addTab( wizard.getContent(), "Install" );
        sheet.getTab( 0 ).setId( "SharkInstallTab" );
        sheet.addTab( manager.getContent(), "Manage" );
        sheet.getTab( 1 ).setId( "SharkManageTab" );
        sheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener()
        {
            @Override
            public void selectedTabChange( TabSheet.SelectedTabChangeEvent event )
            {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab( event.getTabSheet().getSelectedTab() ).getCaption();
                if ( "Manage".equals( caption ) )
                {
                    manager.refreshClustersInfo();
                }
            }
        } );
        verticalLayout.addComponent( sheet );
        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}

