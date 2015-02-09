package org.safehaus.subutai.core.identity.ui.tabs.subviews;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.core.identity.api.Role;
import org.safehaus.subutai.core.identity.api.User;
import org.safehaus.subutai.core.identity.ui.tabs.TabCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


public class UserForm extends VerticalLayout
{

    private TabCallback<BeanItem<User>> callback;

    private static final Logger LOGGER = LoggerFactory.getLogger( UserForm.class );

    private BeanFieldGroup<User> userFieldGroup = new BeanFieldGroup<>( User.class );


    private boolean newValue;

    private TextField username = new TextField( "Username" )
    {
        {
            setInputPrompt( "Username" );
            setEnabled( false );
        }
    };

    private TextField fullName = new TextField( "Full name" )
    {
        {
            setInputPrompt( "Full name" );
        }
    };

    private TextField email = new TextField( "Email" )
    {
        {
            setInputPrompt( "Email" );
            addValidator( new EmailValidator( "Incorrect email format!!!" ) );
        }
    };

    private PasswordField password = new PasswordField( "Password" )
    {
        {
            setInputPrompt( "Password" );
        }
    };

    private PasswordField confirmPassword = new PasswordField( "Confirm password" )
    {
        {
            setInputPrompt( "Confirm password" );
            addValidator( new AbstractStringValidator( "Passwords do not match!!!" )
            {
                @Override
                protected boolean isValidValue( final String value )
                {
                    return password.getValue().equals( value );
                }
            } );
        }
    };

    private TwinColSelect rolesSelector = new TwinColSelect( "User roles" )
    {
        {
            setItemCaptionMode( ItemCaptionMode.PROPERTY );
            setItemCaptionPropertyId( "name" );
            setImmediate( true );
            setSpacing( true );
            setRequired( true );
            setNullSelectionAllowed( false );
        }
    };


    public UserForm( TabCallback<BeanItem<User>> callback, List<Role> roles )
    {
        init();
        BeanContainer<String, Role> permissionsContainer = new BeanContainer<>( Role.class );
        permissionsContainer.setBeanIdProperty( "name" );
        permissionsContainer.addAll( roles );
        rolesSelector.setContainerDataSource( permissionsContainer );
        rolesSelector.setItemCaptionPropertyId( "name" );
        this.callback = callback;
    }


    private void init()
    {
        final Button saveButton = new Button( "Save user", saveListener );
        final Button cancelButton = new Button( "Cancel", cancelListener );
        final Button removeButton = new Button( "Delete user", resetListener );
        saveButton.setStyleName( Reindeer.BUTTON_DEFAULT );

        HorizontalLayout buttons = new HorizontalLayout( saveButton, cancelButton, removeButton );
        buttons.setSpacing( true );

        final FormLayout form = new FormLayout();
        form.addComponents( username, email, fullName, password, confirmPassword, rolesSelector );

        addComponents( form, buttons );

        setSpacing( true );
    }


    public void setUser( final BeanItem<User> user, boolean newValue )
    {
        this.newValue = newValue;
        if ( user != null )
        {
            userFieldGroup.setItemDataSource( user );

            userFieldGroup.bind( username, "username" );
            userFieldGroup.bind( fullName, "fullname" );
            userFieldGroup.bind( email, "email" );
            userFieldGroup.bind( password, "password" );
            confirmPassword.setValue( user.getBean().getPassword() );

            // Pre-select user roles
            User userBean = user.getBean();
            Set<String> roleNames = new HashSet<>();
            for ( final Role role : userBean.getRoles() )
            {
                roleNames.add( role.getName() );
            }
            rolesSelector.setValue( roleNames );

            if ( !newValue )
            {
                userFieldGroup.setReadOnly( true );
                Field<?> fullNameField = userFieldGroup.getField( "fullname" );
                fullNameField.setReadOnly( false );

                Field<?> passwordField = userFieldGroup.getField( "password" );
                passwordField.setReadOnly( false );
            }
            else
            {
                userFieldGroup.setReadOnly( false );
            }
        }
    }


    // When OK button is clicked, commit the form to the bean
    private Button.ClickListener saveListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            // New items have to be added to the container
            // Commit the addition
            try
            {
                userFieldGroup.commit();

                if ( callback != null )
                {
                    Collection<String> selectedRoleNames = ( Collection<String> ) rolesSelector.getValue();
                    User user = userFieldGroup.getItemDataSource().getBean();
                    for ( final String roleName : selectedRoleNames )
                    {
                        BeanItem beanItem = ( BeanItem ) rolesSelector.getItem( roleName );
                        user.addRole( ( Role ) beanItem.getBean() );
                    }
                    callback.saveOperation( userFieldGroup.getItemDataSource(), newValue );
                }
            }
            catch ( FieldGroup.CommitException e )
            {
                LOGGER.error( "Error commit permission fieldGroup changes", e );
                Notification.show( e.getMessage(), Notification.Type.WARNING_MESSAGE );
            }
        }
    };

    private Button.ClickListener resetListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            userFieldGroup.discard();
            if ( callback != null )
            {
                callback.removeOperation( userFieldGroup.getItemDataSource(), newValue );
            }
        }
    };

    private Button.ClickListener cancelListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            userFieldGroup.discard();
            UserForm.this.setVisible( false );
            if ( callback != null )
            {
                callback.cancelOperation();
            }
        }
    };
}
