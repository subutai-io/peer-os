/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.configuration.manager;


import org.safehaus.subutai.configuration.manager.api.ConfigManager;
import org.safehaus.subutai.configuration.manager.manage.CassandraConfigForm;
import org.safehaus.subutai.shared.protocol.Disposable;

import org.apache.log4j.lf5.viewer.configure.ConfigurationManager;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 *
 */
public class ConfigurationManagerForm extends CustomComponent implements Disposable {


    private CassandraConfigForm cassandraConfigForm;


    public ConfigurationManagerForm( ConfigManager configManager ) {
        setHeight( 100, UNITS_PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
//        blueprintManager = new BlueprintLoadForm(environmentManager);
//        environmentForm = new EnvironmentsForm(environmentManager);
        cassandraConfigForm = new CassandraConfigForm(configManager);
        sheet.addTab( cassandraConfigForm.getContentRoot(), "Cassandra cluster" );
//        sheet.addTab( blueprintManager.getContentRoot(), "Blueprint load" );
//        sheet.addTab( environmentForm.getContentRoot(), "Environments" );
        verticalLayout.addComponent( sheet );


        setCompositionRoot( verticalLayout );
    }


    public void dispose() {
    }
}
