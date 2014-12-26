/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.ui.manager.Manager;
import org.safehaus.subutai.plugin.hbase.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class HBaseComponent extends CustomComponent
{

    public HBaseComponent( ExecutorService executor, HBase hBase, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();
        final Manager manager = new Manager( executor, hBase, hadoop, tracker );
        Wizard wizard = new Wizard( executor, hBase, hadoop, tracker, environmentManager );
        sheet.addTab( wizard.getContent(), "Install" );
        sheet.getTab( 0 ).setId( "HbaseInstallTab" );
        sheet.addTab( manager.getContent(), "Manage" );
        sheet.getTab( 1 ).setId( "HbaseManageTab" );

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
                    manager.checkAllNodes();
                }
            }
        } );
        verticalLayout.addComponent( sheet );
        setCompositionRoot( verticalLayout );
//        manager.refreshClustersInfo();
    }
}
