package org.safehaus.subutai.core.identity.ui.tabs.subviews;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.Role;
import org.safehaus.subutai.core.identity.api.User;
import org.safehaus.subutai.core.identity.ui.ErrorUtils;
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

    private final IdentityManager identityManager;
    private TabCallback<BeanItem<User>> callback;

    private static final Logger LOGGER = LoggerFactory.getLogger( UserForm.class );

    private BeanFieldGroup<User> userFieldGroup = new BeanFieldGroup<>( User.class );

    private FormLayout form;

    private boolean newValue;

    Button removeButton = new Button( "Delete user", new Button.ClickListener()
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
    } );

    private TextField username = new TextField( "Username" )
    {
        {
            setRequired( true );
            setInputPrompt( "Username" );
            setRequiredError( "Please enter username." );
            setEnabled( false );
        }
    };

    private TextField fullName = new TextField( "Full name" )
    {
        {
            setRequired( true );
            setInputPrompt( "Full name" );
            setRequiredError( "Please enter full name." );
        }
    };

    private TextField email = new TextField( "Email" )
    {
        {
            setRequired( true );
            setRequiredError( "Please enter e-mail address." );
            setInputPrompt( "Email" );
            addValidator( new EmailValidator( "Incorrect email format!!!" ) );
        }
    };

    private PasswordField password = new PasswordField( "Password" )
    {
        {
            setRequired( true );
            setRequiredError( "Please enter password." );
            setInputPrompt( "Password" );
            addValidator( new AbstractStringValidator( "Passwords do not match." )
            {
                @Override
                protected boolean isValidValue( final String value )
                {
                    return value.equals( confirmPassword.getValue() );
                }
            } );
        }
    };


    private PasswordField confirmPassword = new PasswordField( "Confirm password" )
    {
        {
            setRequired( true );
            setRequiredError( "Please enter password confirm." );
            setInputPrompt( "Confirm password" );
        }
    };


    private TwinColSelect rolesSelector = new TwinColSelect( "User roles" )
    {
        {
            setItemCaptionMode( ItemCaptionMode.PROPERTY );
            setItemCaptionPropertyId( "name" );
            setSpacing( true );
        }
    };
    private final BeanContainer<String, Role> permissionsContainer;


    public UserForm( TabCallback<BeanItem<User>> callback, IdentityManager identityManager )
    {
        init();
        this.identityManager = identityManager;
        permissionsContainer = new BeanContainer<>( Role.class );
        permissionsContainer.setBeanIdProperty( "name" );
        //        permissionsContainer.addAll( roles );
        rolesSelector.setContainerDataSource( permissionsContainer );
        rolesSelector.setItemCaptionPropertyId( "name" );

        this.callback = callback;
    }


    private void init()
    {
        final Button saveButton = new Button( "Save user", saveListener );
        final Button cancelButton = new Button( "Cancel", cancelListener );
        saveButton.setStyleName( Reindeer.BUTTON_DEFAULT );

        HorizontalLayout buttons = new HorizontalLayout( saveButton, cancelButton, removeButton );
        buttons.setSpacing( true );

        form = new FormLayout();
        form.addComponents( username, fullName, email, password, confirmPassword, rolesSelector );

        addComponents( form, buttons );

        setSpacing( true );
    }


    public void setUser( final BeanItem<User> user, boolean newValue )
    {
        this.newValue = newValue;
        if ( user != null )
        {
            List<Role> roles = identityManager.getAllRoles();
            permissionsContainer.removeAllItems();
            permissionsContainer.addAll( roles );
            userFieldGroup.setItemDataSource( user );

            userFieldGroup.bind( username, "username" );
            userFieldGroup.bind( fullName, "fullname" );
            userFieldGroup.bind( email, "email" );
            userFieldGroup.bind( password, "password" );
            confirmPassword.setValue( user.getBean().getPassword() );

            if ( newValue )
            {
                password.setValue( "" );
                confirmPassword.setValue( "" );
            }
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
                username.setReadOnly( true );
                removeButton.setVisible( true );
            }
            else
            {
                username.setReadOnly( false );
                removeButton.setVisible( false );
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

                Collection<String> selectedRoleNames = ( Collection<String> ) rolesSelector.getValue();
                if ( selectedRoleNames.size() < 1 )
                {
                    Notification.show( "Please select at least one role." );
                    return;
                }

                if ( callback != null )
                {
                    User user = userFieldGroup.getItemDataSource().getBean();
                    user.removeAllRoles();

                    for ( final String roleName : selectedRoleNames )
                    {
                        BeanItem beanItem = ( BeanItem ) rolesSelector.getItem( roleName );
                        user.addRole( ( Role ) beanItem.getBean() );
                    }

                    callback.saveOperation( userFieldGroup.getItemDataSource(), newValue );
                    Notification.show( "Successfully saved." );
                }
            }
            catch ( FieldGroup.CommitException e )
            {
                ErrorUtils.showComponentErrors( userFieldGroup.getFields() );
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
