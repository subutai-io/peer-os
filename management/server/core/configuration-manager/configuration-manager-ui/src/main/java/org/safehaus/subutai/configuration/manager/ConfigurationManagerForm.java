/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.configuration.manager;


import org.safehaus.subutai.configuration.manager.api.ConfigManager;
import org.safehaus.subutai.configuration.manager.manage.CassandraClusterConfigForm;
import org.safehaus.subutai.configuration.manager.manage.ConfigLoaderForm;
import org.safehaus.subutai.configuration.manager.manage.HBaseClusterConfigForm;
import org.safehaus.subutai.configuration.manager.manage.HadoopClusterConfigForm;
import org.safehaus.subutai.shared.protocol.Disposable;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 *
 */
public class ConfigurationManagerForm extends CustomComponent implements Disposable {


    private CassandraClusterConfigForm cassandraConfigForm;
    private HadoopClusterConfigForm hadoopClusterConfigForm;
    private HBaseClusterConfigForm hBaseClusterConfigForm;


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
        cassandraConfigForm = new CassandraClusterConfigForm(configManager);
        hadoopClusterConfigForm = new HadoopClusterConfigForm(configManager);
        hBaseClusterConfigForm = new HBaseClusterConfigForm( configManager );
        for ( int i = 0; i < 10; i++ ) {
        sheet.addTab( new ConfigLoaderForm(configManager), "Cassandra" +i );

        }
        sheet.addTab( hadoopClusterConfigForm.getContentRoot(), "Hadoop" );
        sheet.addTab( hBaseClusterConfigForm.getContentRoot(), "HBase" );
//        sheet.addTab( blueprintManager.getContentRoot(), "Blueprint load" );
//        sheet.addTab( environmentForm.getContentRoot(), "Environments" );
        verticalLayout.addComponent( sheet );


        setCompositionRoot( verticalLayout );
    }


    public void dispose() {
    }
}
