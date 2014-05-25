/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.cassandra;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.subutai.ui.cassandra.manager.Manager;
import org.safehaus.subutai.ui.cassandra.wizard.Wizard;

/**
 * @author dilshat
 */
public class CassandraForm extends CustomComponent {

    private final Wizard wizard;
    private final Manager manager;

    public CassandraForm() {
        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();
        TabSheet sheet = new TabSheet();
        sheet.setStyleName(Runo.TABSHEET_SMALL);
        sheet.setSizeFull();
        manager = new Manager();
        wizard = new Wizard();
        sheet.addTab(wizard.getContent(), "Install");
        sheet.addTab(manager.getContent(), "Manage");
        verticalLayout.addComponent(sheet);
        setCompositionRoot(verticalLayout);
        manager.refreshClustersInfo();
    }

}
