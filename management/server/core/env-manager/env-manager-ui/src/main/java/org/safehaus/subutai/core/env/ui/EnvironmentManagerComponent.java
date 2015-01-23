package org.safehaus.subutai.core.env.ui;


import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


public class EnvironmentManagerComponent extends CustomComponent implements Disposable
{

    private EnvironmentManager environmentManager;
    private PeerManager peerManager;


    public EnvironmentManagerComponent( final EnvironmentManager environmentManager, final PeerManager peerManager )
    {
        this.environmentManager = environmentManager;
        this.peerManager = peerManager;

        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();


        //TODO fill UI here

        verticalLayout.addComponent( sheet );

        setCompositionRoot( verticalLayout );
    }


    @Override
    public void dispose()
    {
        //not ready yet
    }
}
