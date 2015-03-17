package org.safehaus.subutai.core.identity.ui.tabs;


import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.User;
import org.safehaus.subutai.core.identity.ui.tabs.subviews.UserForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


/**
 * Created by talas on 1/26/15.
 */
public class UsersTab extends CustomComponent implements TabCallback<BeanItem<User>>
{
    private enum FormState
    {
        STATE_EXISTING_ENTITY_SELECTED,
        STATE_SAVE_EXISTING_ENTITY,
        STATE_SAVE_NEW_ENTITY,
        STATE_REMOVE_ENTITY,
        STATE_NEW_ENTITY,
        STATE_CANCEL
    }


    private IdentityManager identityManager;
    private Table usersTable;
    private BeanItemContainer<User> beans;
    private Button newBean;
    private UserForm form;

    private static final Logger LOGGER = LoggerFactory.getLogger( UsersTab.class );


    public UsersTab( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
        editorForm();
    }


    void editorForm()
    {
        VerticalLayout vlayout = new VerticalLayout();

        // Create a container for such beans
        // Add some beans to it
        beans = new BeanItemContainer<>( User.class );
        beans.addAll( identityManager.getAllUsers() );

        // A layout for the table and form
        HorizontalLayout layout = new HorizontalLayout();

        // Bind a table to it
        usersTable = new Table( "Users", beans );
        usersTable.setVisibleColumns( new Object[] { "username", "fullname", "email" } );
        usersTable.setPageLength( 7 );
        usersTable.setColumnHeader( "username", "Username" );
        usersTable.setColumnHeader( "fullname", "Full name" );
        usersTable.setColumnHeader( "email", "Email" );
        usersTable.setBuffered( false );

        // Create a form for editing a selected or new item.
        // It is invisible until actually used.
        form = new UserForm( this, identityManager.getAllRoles() );
        form.setVisible( false );

        // When the user selects an item, show it in the form
        usersTable.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                // Close the form if the item is deselected
                if ( event.getProperty().getValue() == null )
                {
                    form.setVisible( false );
                    return;
                }
                BeanItem<User> userBean = beans.getItem( usersTable.getValue() );
                form.setUser( userBean, false );
                refreshControls( FormState.STATE_EXISTING_ENTITY_SELECTED );
                //                usersTable.select( null );
            }
        } );
        usersTable.setSelectable( true );
        usersTable.setImmediate( true );

        // Creates a new bean for editing in the form before adding
        // it to the table. Adding is handled after committing
        // the form.
        newBean = new Button( "New User" );
        newBean.addClickListener( new Button.ClickListener()
        {
            public void buttonClick( Button.ClickEvent event )
            {

                // Create a new item; this will create a new bean
                BeanItem<User> newUser = new BeanItem<>( identityManager.createMockUser( "", "", "", "" ) );

                // The form was opened for editing a new item
                refreshControls( FormState.STATE_NEW_ENTITY );


                // Make the form a bit nicer
                //this is an example for future how to improve UI
                form.setUser( newUser, true );
            }
        } );

        layout.addComponent( usersTable );
        layout.addComponent( form );

        layout.setSpacing( true );

        vlayout.addComponent( layout );
        vlayout.addComponent( newBean );

        setCompositionRoot( vlayout );
    }


    //make centralized modification and controls updates
    //state:
    //  0: existing entity selected in table
    //  1: on save event existing one
    //  2: on save event new entity
    //  3: on remove event
    //  4: on new entity button click
    private void refreshControls( FormState state )
    {
        switch ( state )
        {
            case STATE_EXISTING_ENTITY_SELECTED:
                newBean.setEnabled( false );
                form.setVisible( true );
                break;
            case STATE_SAVE_EXISTING_ENTITY:
                newBean.setEnabled( false );
                break;
            case STATE_SAVE_NEW_ENTITY:
                newBean.setEnabled( true );
                usersTable.setEnabled( true );
                form.setVisible( false );
                break;
            case STATE_REMOVE_ENTITY:
                newBean.setEnabled( true );
                form.setVisible( false );
                break;
            case STATE_NEW_ENTITY:
                form.setVisible( true );
                newBean.setEnabled( false );
                usersTable.setEnabled( false );
                //                usersTable.select( null );
                break;
            case STATE_CANCEL:
                form.setVisible( false );
                newBean.setEnabled( true );
                usersTable.setEnabled( true );
                break;
        }
    }


    @Override
    public void saveOperation( final BeanItem<User> value, final boolean newValue )
    {
        identityManager.updateUser( value.getBean() );
        if ( newValue )
        {
            beans.addBean( value.getBean() );
            refreshControls( FormState.STATE_SAVE_NEW_ENTITY );
        }
        else
        {
            refreshControls( FormState.STATE_SAVE_EXISTING_ENTITY );
        }
    }


    @Override
    public void removeOperation( final BeanItem<User> value, final boolean newValue )
    {
        if ( !newValue )
        {
            identityManager.deleteUser( value.getBean() );
            beans.removeItem( value.getBean() );
        }
        refreshControls( FormState.STATE_REMOVE_ENTITY );
    }


    @Override
    public void cancelOperation()
    {
        refreshControls( FormState.STATE_CANCEL );
    }
}
