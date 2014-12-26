/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.ui.manager.Manager;
import org.safehaus.subutai.plugin.accumulo.ui.wizard.Wizard;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class AccumuloComponent extends CustomComponent
{

    public AccumuloComponent( ExecutorService executorService, Accumulo accumulo, Hadoop hadoop, Zookeeper zookeeper, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {

        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();

        final Manager manager = new Manager( executorService, accumulo, hadoop, tracker, environmentManager );
        Wizard wizard = new Wizard( executorService, accumulo, hadoop, zookeeper, tracker, environmentManager );
        sheet.addTab( wizard.getContent(), "Install" );
        sheet.getTab( 0 ).setId( "AccumuloInstallTab" );
        sheet.addTab( manager.getContent(), "Manage" );
        sheet.getTab( 1 ).setId( "AccumuloManageTab" );
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
                    manager.checkAll();
                }
            }
        } );
        verticalLayout.addComponent( sheet );
        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}
