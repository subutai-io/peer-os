package org.safehaus.subutai.plugin.spark.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.ui.manager.Manager;
import org.safehaus.subutai.plugin.spark.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class SparkComponent extends CustomComponent
{

    public SparkComponent( ExecutorService executor, Spark spark, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sparkSheet = new TabSheet();
        sparkSheet.setSizeFull();
        final Manager manager = new Manager( executor, spark, hadoop, tracker, environmentManager );
        Wizard wizard = new Wizard( executor, spark, hadoop, tracker, environmentManager );
        sparkSheet.addTab( wizard.getContent(), "Install" );
        sparkSheet.getTab( 0 ).setId( "SparkInstallTab" );
        sparkSheet.addTab( manager.getContent(), "Manage" );
        sparkSheet.getTab( 1 ).setId( "SparkManageTab" );
        sparkSheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener()
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
        verticalLayout.addComponent( sparkSheet );
        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}
