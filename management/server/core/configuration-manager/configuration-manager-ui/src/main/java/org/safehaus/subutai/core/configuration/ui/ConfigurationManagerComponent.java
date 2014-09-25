/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.configuration.ui;


import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.configuration.api.ConfigurationManager;
import org.safehaus.subutai.core.configuration.ui.manage.ConfigLoaderForm;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 *
 */
public class ConfigurationManagerComponent extends CustomComponent implements Disposable
{


    public ConfigurationManagerComponent( ConfigurationManager configurationManager )
    {
        setHeight( 100, UNITS_PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
        sheet.addTab( new ConfigLoaderForm( configurationManager ), "Configuration" );
        verticalLayout.addComponent( sheet );

        setCompositionRoot( verticalLayout );
    }


    public void dispose()
    {
        //TODO Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
        // complete the implementation.
    }
}
