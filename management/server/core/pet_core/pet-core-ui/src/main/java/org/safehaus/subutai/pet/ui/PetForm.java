package org.safehaus.subutai.pet.ui;


import org.safehaus.subutai.common.protocol.Disposable;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PetForm extends CustomComponent implements Disposable {

    public PetForm() {
        setHeight( 100, UNITS_PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
        sheet.addTab( new Button("test beta new"), "Environments" );
        verticalLayout.addComponent( sheet );


        setCompositionRoot( verticalLayout );
    }


    @Override
    public void dispose() {

    }
}
