package org.safehaus.subutai.core.identity.ui;


import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.ui.tabs.RoleManagementForm;
import org.safehaus.subutai.core.identity.ui.tabs.UserManagementForm;
import org.safehaus.subutai.core.identity.ui.tabs.UserRegistrationForm;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 * Created by talas on 1/21/15.
 */
public class IdentityManagerComponent extends CustomComponent implements Disposable
{

    private UserManagementForm userManagementForm;
    private UserRegistrationForm userRegistrationForm;
    private RoleManagementForm roleManagementForm;


    public IdentityManagerComponent( final IdentityManagerPortalModule portalModule, IdentityManager identityManager )
    {
        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
        userManagementForm = new UserManagementForm( identityManager );
        userRegistrationForm = new UserRegistrationForm( identityManager );
        userRegistrationForm.init();
        roleManagementForm = new RoleManagementForm( identityManager );

        sheet.addTab( userManagementForm, "User management space." );
        sheet.getTab( 0 ).setId( "UserManagement" );

        sheet.addTab( userRegistrationForm, "New user registration space." );
        sheet.getTab( 1 ).setId( "UserRegistration" );

        sheet.addTab( roleManagementForm, "Role editing/creation space." );
        sheet.getTab( 2 ).setId( "UserRegistration" );

        verticalLayout.addComponent( sheet );


        setCompositionRoot( verticalLayout );
    }


    @Override
    public void dispose()
    {

    }
}
