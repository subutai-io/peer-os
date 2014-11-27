package org.safehaus.subutai.plugin.hadoop.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.ui.manager.Manager;
import org.safehaus.subutai.plugin.hadoop.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class HadoopComponent extends CustomComponent
{
    private final Wizard wizard;
    private final Manager manager;


    public HadoopComponent( ExecutorService executorService, Tracker tracker, Hadoop hadoop, EnvironmentManager environmentManager, HostRegistry hostRegistry ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();

        manager = new Manager( executorService, tracker, hadoop, environmentManager );
        wizard = new Wizard( executorService, hadoop, hostRegistry, tracker );
        sheet.addTab( wizard.getContent(), "Install" );
        sheet.getTab( 0 ).setId( "HadoopInstallTab" );
        sheet.addTab( manager.getContent(), "Manage" );
        sheet.getTab( 1 ).setId( "HadoopManageTab" );


        verticalLayout.addComponent( sheet );
        setCompositionRoot( verticalLayout );
    }
}
