/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui;


import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.hbase.ui.manager.Manager;
import org.safehaus.subutai.plugin.hbase.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import javax.naming.NamingException;
import java.util.concurrent.ExecutorService;


public class HBaseForm extends CustomComponent
{

    public HBaseForm( ExecutorService executor, ServiceLocator serviceLocator ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();
        final Manager manager = new Manager( executor, serviceLocator );
        Wizard wizard = new Wizard( executor, serviceLocator );
        sheet.addTab( wizard.getContent(), "Install" );
        sheet.addTab( manager.getContent(), "Manage" );
        sheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange( TabSheet.SelectedTabChangeEvent event ) {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab( event.getTabSheet().getSelectedTab() ).getCaption();
                if( caption.equals( "Manage" ) ) {
                    manager.refreshClustersInfo();
                    manager.checkAllNodes();
                }
            }
        } );
        verticalLayout.addComponent( sheet );
        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}
