package io.subutai.core.identity.ui;


import io.subutai.common.protocol.Disposable;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.ui.tabs.RolesTab;
import io.subutai.core.identity.ui.tabs.TokensTab;
import io.subutai.core.identity.ui.tabs.UsersTab;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


public class IdentityManagerComponent extends CustomComponent implements Disposable
{
    public IdentityManagerComponent( final IdentityManagerPortalModule portalModule, IdentityManager identityManager )
    {
        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();

        // TODO uncomment when permissions will be fully implemented
        //      PermissionsTab permissionsTab = new PermissionsTab( identityManager );
        UsersTab usersTab = new UsersTab( identityManager );
        RolesTab rolesTab = new RolesTab( identityManager );
        TokensTab tokensTab = new TokensTab( identityManager );

        sheet.addTab( usersTab, "User management space." ).setId( "UsersTab" );

        sheet.addTab( rolesTab, "Roles management space" ).setId( "RolesManagement" );
        // TODO uncomment when permissions will be fully implemented
        //        sheet.addTab( permissionsTab, "Permission edition space." ).setId( "PermissionsTab" );

        verticalLayout.addComponent( sheet );

        setCompositionRoot( verticalLayout );
    }


    @Override
    public void dispose()
    {

    }
}
