/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui;


import org.safehaus.subutai.plugin.hbase.ui.manager.Manager;
import org.safehaus.subutai.plugin.hbase.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


/**
 * @author dilshat
 */
public class HBaseForm extends CustomComponent {


    public HBaseForm() {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet mongoSheet = new TabSheet();
        mongoSheet.setSizeFull();
        final Manager manager = new Manager();
        final Wizard wizard = new Wizard();
        mongoSheet.addTab( wizard.getContent(), "Install" );
        mongoSheet.addTab( manager.getContent(), "Manage" );

        verticalLayout.addComponent( mongoSheet );
        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}
