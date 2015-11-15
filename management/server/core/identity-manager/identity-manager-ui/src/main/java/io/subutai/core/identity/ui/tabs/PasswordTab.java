package io.subutai.core.identity.ui.tabs;


import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;

import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;


public class PasswordTab extends CustomComponent
{
    public PasswordTab( final IdentityManager identityManager )
    {
        VerticalLayout content = new VerticalLayout();
        content.setSpacing( true );
        content.setMargin( true );
        final PasswordField oldPassword = new PasswordField( "Old Password" );
        oldPassword.setRequired( true );
        final PasswordField newPassword = new PasswordField( "New Password" );
        newPassword.setRequired( true );
        PasswordField confirm = new PasswordField( "Confirm Password" );
        confirm.setRequired( true );
        Button change = new Button( "Change" );
        change.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                User user = identityManager.getLoggedUser();

                // on success
                if ( identityManager
                        .changeUserPassword( user.getId(), oldPassword.getValue(), newPassword.getValue() ) )
                {
                    Notification notif = new Notification( "Password successfully changed" );
                    notif.setDelayMsec( 2000 );
                    notif.show( Page.getCurrent() );
                }
                else
                {
                    Notification notif = new Notification( "Error setting password.Old password is invalid!" );
                    notif.setDelayMsec( 2000 );
                    notif.show( Page.getCurrent() );
                }
            }
        } );
        content.addComponent( oldPassword );
        content.addComponent( newPassword );
        content.addComponent( confirm );
        content.addComponent( change );
        content.setComponentAlignment( oldPassword, Alignment.BOTTOM_LEFT );
        content.setComponentAlignment( newPassword, Alignment.BOTTOM_LEFT );
        content.setComponentAlignment( confirm, Alignment.BOTTOM_LEFT );
        this.setCompositionRoot( content );
    }
}
