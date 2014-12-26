package org.safehaus.subutai.plugin.pig.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.pig.api.Pig;
import org.safehaus.subutai.plugin.pig.ui.manager.Manager;
import org.safehaus.subutai.plugin.pig.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class PigComponent extends CustomComponent
{

    public PigComponent( ExecutorService executorService, Pig pig, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();
        final Manager manager = new Manager( executorService, pig, hadoop, tracker, environmentManager );
        Wizard wizard = new Wizard( executorService, pig, hadoop, tracker, environmentManager );
        sheet.addTab( wizard.getContent(), "Install" );
        sheet.getTab( 0 ).setId( "PigInstallTab" );
        sheet.addTab( manager.getContent(), "Manage" );
        sheet.getTab( 1 ).setId( "PigManageTab" );
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

        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}
