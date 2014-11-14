package org.safehaus.subutai.wol.ui;


import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.subutai.common.protocol.Disposable;


/**
 * Created by emin on 14/11/14.
 */
public class WolForm extends CustomComponent implements Disposable {

    public WolForm() {
        setHeight( 100, UNITS_PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
        sheet.addTab( new Button("SendMagicPackage"), "WakeOnLAN" );
        verticalLayout.addComponent( sheet );
        setCompositionRoot( verticalLayout );
    }


    @Override
    public void dispose() {

    }
}
