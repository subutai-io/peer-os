package org.safehaus.subutai.plugin.sqoop.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.sqoop.api.Sqoop;
import org.safehaus.subutai.plugin.sqoop.ui.manager.ExportPanel;
import org.safehaus.subutai.plugin.sqoop.ui.manager.ImportExportBase;
import org.safehaus.subutai.plugin.sqoop.ui.manager.ImportPanel;
import org.safehaus.subutai.plugin.sqoop.ui.manager.Manager;
import org.safehaus.subutai.plugin.sqoop.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class SqoopComponent extends CustomComponent
{

    private final Wizard wizard;
    private final Manager manager;

    private final TabSheet sheet;


    public SqoopComponent( ExecutorService executorService, Sqoop sqoop, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {
        manager = new Manager( executorService, sqoop, hadoop, tracker, environmentManager, this );
        wizard = new Wizard( executorService, sqoop, hadoop, tracker, environmentManager );

        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        sheet = new TabSheet();
        sheet.setSizeFull();
        sheet.addTab( wizard.getContent(), "Install" );
        sheet.getTab( 0 ).setId( "SqoopInstallTab" );
        sheet.addTab( manager.getContent(), "Manage" );
        sheet.getTab( 1 ).setId( "SqoopManageTab" );
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


    public void addTab( ImportExportBase component )
    {
        TabSheet.Tab tab = sheet.addTab( component );
        if ( component instanceof ExportPanel )
        {
            tab.setCaption( "Export" );
        }
        else if ( component instanceof ImportPanel )
        {
            tab.setCaption( "Import" );
        }
        sheet.setSelectedTab( component );
    }
}
