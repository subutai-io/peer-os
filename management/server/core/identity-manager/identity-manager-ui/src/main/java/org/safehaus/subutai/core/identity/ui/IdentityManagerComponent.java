package org.safehaus.subutai.core.identity.ui;


import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.ui.tabs.PermissionsTab;
import org.safehaus.subutai.core.identity.ui.tabs.RolesTab;
import org.safehaus.subutai.core.identity.ui.tabs.UsersTab;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 * Created by talas on 1/21/15.
 */
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

        PermissionsTab permissionsTab = new PermissionsTab( identityManager );
        UsersTab usersTab = new UsersTab( identityManager );
        RolesTab rolesTab = new RolesTab( identityManager );

        sheet.addTab( usersTab, "User management space." ).setId( "UsersTab" );

        sheet.addTab( rolesTab, "Roles management space" ).setId( "RolesManagement" );

        sheet.addTab( permissionsTab, "Permission edition space." ).setId( "PermissionsTab" );

        verticalLayout.addComponent( sheet );

        setCompositionRoot( verticalLayout );
    }


    @Override
    public void dispose()
    {

    }
}
