package org.safehaus.subutai.core.peer.ui;


import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.peer.ui.forms.PeerRegisterForm;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PeerForm extends CustomComponent implements Disposable
{

    private PeerManagerPortalModule peerManagerPortalModule;


    public PeerForm( PeerManagerPortalModule peerManagerPortalModule )
    {
        setHeight( 100, UNITS_PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
        sheet.addTab( new PeerRegisterForm( peerManagerPortalModule ), "Registration" );
        verticalLayout.addComponent( sheet );


        setCompositionRoot( verticalLayout );
    }


    @Override
    public void dispose()
    {
        peerManagerPortalModule = null;
    }
}
