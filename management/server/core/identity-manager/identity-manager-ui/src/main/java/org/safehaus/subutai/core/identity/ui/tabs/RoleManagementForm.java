package org.safehaus.subutai.core.identity.ui.tabs;


import org.safehaus.subutai.common.protocol.Disposable;

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



    public RoleManagementForm()
    {
        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        PermissionsTab permissionsTab = new PermissionsTab();
        RolesTab rolesTab = new RolesTab();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();

        sheet.addTab( permissionsTab, "System Permissions" );
        sheet.getTab( 0 ).setId( "SystemPermissions" );

        sheet.addTab( rolesTab, "System Roles" );
        sheet.getTab( 1 ).setId( "SystemRoles" );

        verticalLayout.addComponent( sheet );

        setCompositionRoot( verticalLayout );
    }

    @Override
    public void dispose()
    {

    }
}
