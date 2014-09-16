/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.cassandra.ui;


import org.safehaus.subutai.plugin.cassandra.ui.manager.Manager;
import org.safehaus.subutai.plugin.cassandra.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class CassandraForm extends CustomComponent {

    private final Wizard wizard;
    private final Manager manager;


    public CassandraForm(CassandraUI cassandraUI) {

        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        setCompositionRoot( verticalLayout );

        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();
        manager = new Manager(cassandraUI);
        wizard = new Wizard(cassandraUI);
        sheet.addTab( wizard.getContent(), "Install" );
        //		sheet.addTab(new ConfigurationView(), "Configure");
        sheet.addTab( manager.getContent(), "Manage" );
        sheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange( TabSheet.SelectedTabChangeEvent event ) {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab( event.getTabSheet().getSelectedTab() ).getCaption();
                if( caption.equals( "Manage" ) ) {
                    manager.refreshClustersInfo();
                }
            }
        } );
        verticalLayout.addComponent( sheet );
    }



}
