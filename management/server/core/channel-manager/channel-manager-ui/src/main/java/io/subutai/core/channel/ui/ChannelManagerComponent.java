package io.subutai.core.channel.ui;


import org.safehaus.subutai.common.protocol.Disposable;
import io.subutai.core.channel.api.ChannelManager;
import io.subutai.core.channel.ui.tabs.PortManager;
import io.subutai.core.channel.ui.tabs.UserTokenManager;
import org.safehaus.subutai.core.identity.api.IdentityManager;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


public class ChannelManagerComponent extends CustomComponent implements Disposable
{
    public ChannelManagerComponent ( final ChannelManagerPortalModule portalModule, ChannelManager channelManager , IdentityManager identityManager)
    {
        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();

        UserTokenManager userTokenManager = new UserTokenManager( channelManager,identityManager );
        PortManager portManager = new PortManager( channelManager );

        sheet.addTab( userTokenManager, "User Token Manager" ).setId( "UserTokenManagerTab" );


        //--------------------------------------------------------------------------------------------
        if(identityManager.getUser().isAdmin())
        {
            sheet.addTab( portManager, "Port Manager" ).setId( "PortManager" );
        }
        //--------------------------------------------------------------------------------------------

        verticalLayout.addComponent( sheet );

        setCompositionRoot( verticalLayout );
    }

    @Override
    public void dispose()
    {

    }
}