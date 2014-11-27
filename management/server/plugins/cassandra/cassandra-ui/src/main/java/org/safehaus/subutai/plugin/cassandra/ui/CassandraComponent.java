package org.safehaus.subutai.plugin.cassandra.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.ui.Environment.EnvironmentWizard;
import org.safehaus.subutai.plugin.cassandra.ui.manager.Manager;
import org.safehaus.subutai.plugin.cassandra.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class CassandraComponent extends CustomComponent
{

    private final Wizard wizard;
    private final EnvironmentWizard environmentWizard;
    private final Manager manager;


    public CassandraComponent( ExecutorService executorService, Cassandra cassandra, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        setCompositionRoot( verticalLayout );

        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();

        manager = new Manager( executorService, cassandra, tracker, environmentManager );
        wizard = new Wizard( executorService, cassandra, tracker );
        environmentWizard = new EnvironmentWizard( executorService, cassandra, tracker, environmentManager );
        sheet.addTab( wizard.getContent(), "Install" );
        sheet.getTab( 0 ).setId( "CassandraInstallTab" );
        sheet.addTab( environmentWizard.getContent(), "Configure environment" );
        sheet.getTab( 1 ).setId( "CassandraConfigureEnviromentTab" );
        sheet.addTab( manager.getContent(), "Manage" );
        sheet.getTab( 2 ).setId( "CassandraManageTab" );
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
        //manager.refreshClustersInfo();
    }
}
