package org.safehaus.subutai.plugin.spark.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.spark.ui.manager.Manager;
import org.safehaus.subutai.plugin.spark.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class SparkForm extends CustomComponent {

    public SparkForm( ExecutorService executor, ServiceLocator serviceLocator ) throws NamingException {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet mongoSheet = new TabSheet();
        mongoSheet.setSizeFull();
        Manager manager = new Manager( executor, serviceLocator );
        Wizard wizard = new Wizard( executor, serviceLocator );
        mongoSheet.addTab( wizard.getContent(), "Install" );
        mongoSheet.addTab( manager.getContent(), "Manage" );
        verticalLayout.addComponent( mongoSheet );

        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}
