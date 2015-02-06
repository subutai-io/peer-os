package org.safehaus.subutai.core.identity.ui.tabs;


import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.identity.api.IdentityManager;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 * Created by talas on 1/21/15.
 */
public class RoleManagementForm extends CustomComponent implements Disposable
{
    //Permissions
    //Roles associated with permissions
    private IdentityManager identityManager;


    public RoleManagementForm( IdentityManager identityManager )
    {
        this.identityManager = identityManager;
        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        PermissionsTab permissionsTab = new PermissionsTab( this.identityManager );
        UsersTab usersTab = new UsersTab( this.identityManager );
        RolesTabOld rolesTabOld = new RolesTabOld();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();

        sheet.addTab( permissionsTab, "System Permissions" );
        sheet.getTab( 0 ).setId( "SystemPermissions" );

        sheet.addTab( rolesTabOld, "System Roles" );
        sheet.getTab( 1 ).setId( "SystemRoles" );

        sheet.addTab( usersTab, "System Users" );
        sheet.getTab( 2 ).setId( "SystemUsers" );

        verticalLayout.addComponent( sheet );

        setCompositionRoot( verticalLayout );
    }

    @Override
    public void dispose()
    {

    }
}
