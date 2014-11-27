package org.safehaus.subutai.plugin.presto.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.ui.manager.Manager;
import org.safehaus.subutai.plugin.presto.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class PrestoComponent extends CustomComponent
{

    public PrestoComponent( ExecutorService executorService, Presto presto, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet cassandraSheet = new TabSheet();
        cassandraSheet.setSizeFull();
        final Manager manager = new Manager( executorService, presto, hadoop, tracker, environmentManager );
        Wizard wizard = new Wizard( executorService, presto, hadoop, tracker, environmentManager );
        cassandraSheet.addTab( wizard.getContent(), "Install" );
        cassandraSheet.getTab( 0 ).setId( "PrestoInstallTab" );
        cassandraSheet.addTab( manager.getContent(), "Manage" );
        cassandraSheet.getTab( 1 ).setId( "PrestoManageTab" );
        cassandraSheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener()
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
        verticalLayout.addComponent( cassandraSheet );
        verticalLayout.addComponent( cassandraSheet );

        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}
