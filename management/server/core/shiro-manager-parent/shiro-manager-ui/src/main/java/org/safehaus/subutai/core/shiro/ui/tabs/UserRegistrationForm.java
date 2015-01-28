package org.safehaus.subutai.core.shiro.ui.tabs;


import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


/**
 * Created by talas on 1/21/15.
 */

public class UserRegistrationForm extends CustomComponent
{
    private static final long serialVersionUID = -1770451668233870037L;


    public void init()
    {
        VerticalLayout layout = new VerticalLayout();

        explicit( layout );
        if ( getCompositionRoot() == null )
        {
            setCompositionRoot( layout );
        }
    }


    void explicit( final VerticalLayout layout )
    {
        // A nameSurname with automatic validation disabled
        final TextField nameSurname = new TextField( "Name" );
        nameSurname.setValidationVisible( false );

        // Define validation
        nameSurname.addValidator(
                new StringLengthValidator( "Name/Surname must be 1-10 letters (was {0})", 1, 10, true ) );

        final TextField username = new TextField( "Username" );
        username.setValidationVisible( false );

        // Define validation
        //TODO maybe some validator for username uniqueness
        username.addValidator(
                new StringLengthValidator( "The username must be 1-10 letters (was {0})", 1, 10, true ) );

        final PasswordField passwordField = new PasswordField( "Password" );
        passwordField.setValidationVisible( false );

        // Define validation
        //TODO password strength validation
        passwordField.addValidator(
                new StringLengthValidator( "The password must be 1-10 letters (was {0})", 1, 10, true ) );

        final PasswordField repeatPasswordField = new PasswordField( "Password" );
        repeatPasswordField.setValidationVisible( false );

        // Define validation
        //TODO passwords match validation
        repeatPasswordField.addValidator( new AbstractStringValidator( "Passwords doesn't match" )
        {
            @Override
            protected boolean isValidValue( final String value )
            {
                return value.equals( passwordField.getValue() );
            }
        } );

        final TextField email = new TextField( "Email address" );
        email.setValidationVisible( false );

        // Define validation
        email.addValidator( new EmailValidator( "Wrong email address" ) );

        ComboBox userRole = new ComboBox( "User system-wide role" );
        userRole.setImmediate( true );
        userRole.setTextInputAllowed( false );
        userRole.setRequired( true );
        userRole.setNullSelectionAllowed( false );
        userRole.addItem( "Admin" );
        userRole.addItem( "User" );

        // Run validation
        Button validate = new Button( "Save" );
        validate.addClickListener( new ClickListener()
        {
            private static final long serialVersionUID = 7729516791241492195L;


            @Override
            public void buttonClick( ClickEvent event )
            {
                try
                {
                    nameSurname.validate();
                    username.validate();
                    passwordField.validate();
                    repeatPasswordField.validate();
                    email.validate();
                }
                catch ( InvalidValueException e )
                {
                    Notification.show( e.getMessage() );
                }
            }
        } );

        layout.addComponent( nameSurname );
        layout.addComponent( username );
        layout.addComponent( passwordField );
        layout.addComponent( repeatPasswordField );
        layout.addComponent( email );
        layout.addComponent( userRole );
        layout.addComponent( validate );
    }
}
