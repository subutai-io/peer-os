package io.subutai.core.peer.ui;


import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

import io.subutai.common.protocol.Disposable;
import io.subutai.core.peer.ui.container.ContainerComponent;
import io.subutai.core.peer.ui.forms.RegistrationForm;


public class PeerComponent extends CustomComponent implements Disposable
{
    protected static final Logger LOG = LoggerFactory.getLogger( PeerComponent.class );


    public PeerComponent( PeerManagerPortalModule peerManagerPortalModule )
    {
        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
        try
        {
            sheet.addTab( new ContainerComponent( peerManagerPortalModule ), "Containers" );
        }
        catch ( NamingException e )
        {
            LOG.error( "Could not create container component.", e );
        }
//        sheet.addTab( new PeerRegisterForm( peerManagerPortalModule ), "Registration" );
        sheet.addTab( new RegistrationForm( peerManagerPortalModule ), "Registration" );

        verticalLayout.addComponent( sheet );


        setCompositionRoot( verticalLayout );
    }


    @Override
    public void dispose()
    {

    }
}
