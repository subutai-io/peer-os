package org.safehaus.subutai.core.peer.ui;


import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.peer.ui.container.ContainerComponent;
import org.safehaus.subutai.core.peer.ui.forms.PeerGroupComponent;
import org.safehaus.subutai.core.peer.ui.forms.PeerRegisterForm;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PeerComponent extends CustomComponent implements Disposable
{


    public PeerComponent( PeerManagerPortalModule peerManagerPortalModule )
    {
        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
        sheet.addTab( new PeerRegisterForm( peerManagerPortalModule ), "Registration" );
        sheet.addTab( new PeerGroupComponent( peerManagerPortalModule ), "Peer groups" );
        try
        {
            sheet.addTab( new ContainerComponent( peerManagerPortalModule ), "Containers" );
        }
        catch ( NamingException e )
        {
            peerManagerPortalModule.LOG.error( "Could not create container component.", e );
        }

        verticalLayout.addComponent( sheet );


        setCompositionRoot( verticalLayout );
    }


    @Override
    public void dispose()
    {
        final PeerManagerPortalModule peerManagerPortalModule = null;
    }
}
