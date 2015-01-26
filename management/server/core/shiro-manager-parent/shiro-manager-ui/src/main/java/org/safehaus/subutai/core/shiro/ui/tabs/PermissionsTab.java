package org.safehaus.subutai.core.shiro.ui.tabs;


import java.io.Serializable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


/**
 * Created by talas on 1/26/15.
 */
public class PermissionsTab extends CustomComponent
{
    public PermissionsTab()
    {
        editorForm();
        // TODO still need some modifications
    }


    public class Permission implements Serializable
    {
        String name;


        public Permission( String name )
        {
            this.name = name;
        }


        public String getName()
        {
            return name;
        }


        public void setName( String name )
        {
            this.name = name;
        }
    }


    void editorForm()
    {
        VerticalLayout vlayout = new VerticalLayout();

        // Create a container for such beans
        final BeanItemContainer<Permission> beans = new BeanItemContainer<>( Permission.class );

        // Add some beans to it
        //TODO need to retrieve all permissions from db.
        beans.addBean( new Permission( "CRUD permissions" ) );
        beans.addBean( new Permission( "Read permissions" ) );
        beans.addBean( new Permission( "Update permissions" ) );

        // A layout for the table and form
        HorizontalLayout layout = new HorizontalLayout();

        // Bind a table to it
        final Table table = new Table( "Permissions", beans );
        table.setVisibleColumns( new Object[] { "name" } );
        table.setPageLength( 7 );
        table.setBuffered( false );

        // Create a form for editing a selected or new item.
        // It is invisible until actually used.
        final Form form = new Form();
        form.setCaption( "Edit Permission" );
        form.setVisible( false );
        form.setBuffered( true );


        // When the user selects an item, show it in the form
        table.addValueChangeListener( new Property.ValueChangeListener()
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

                // Bind the form to the selected item
                form.setItemDataSource( table.getItem( table.getValue() ) );
                form.setVisible( true );

                // The form was opened for editing an existing item
                table.setData( null );
            }
        } );
        table.setSelectable( true );
        table.setImmediate( true );

        // Creates a new bean for editing in the form before adding
        // it to the table. Adding is handled after committing
        // the form.
        final Button newBean = new Button( "New Permission" );
        newBean.addClickListener( new Button.ClickListener()
        {
            public void buttonClick( Button.ClickEvent event )
            {
                Permission newPermission = new Permission( "Some permission" );
                // Create a new item; this will create a new bean
                Object itemId = beans.addItem( newPermission );

                // The form was opened for editing a new item
                table.setData( itemId );

                table.select( itemId );
                table.setEnabled( false );
                newBean.setEnabled( false );

                // Make the form a bit nicer
                //this is an example for future how to improve UI
                // form.setVisibleItemProperties( new Object[] { "name" } );
                form.setItemDataSource( beans.getItem( newPermission ) );
                form.setVisible( true );
            }
        } );

        // When OK button is clicked, commit the form to the bean
        final Button submit = new Button( "Save" );
        submit.addClickListener( new Button.ClickListener()
        {
            public void buttonClick( Button.ClickEvent event )
            {
                form.commit();
                form.setVisible( false ); // and close it

                // New items have to be added to the container
                // Commit the addition
                table.commit();

                table.setEnabled( true );
                newBean.setEnabled( true );
            }
        } );

        // Make modification to enable/disable the Save button
        form.setFormFieldFactory( new DefaultFieldFactory()
        {
            @Override
            public Field createField( Item item, Object propertyId, Component uiContext )
            {
                final AbstractField field = ( AbstractField ) super.createField( item, propertyId, uiContext );
                field.addValueChangeListener( new Property.ValueChangeListener()
                {
                    @Override
                    public void valueChange( Property.ValueChangeEvent event )
                    {
                        submit.setEnabled( form.isModified() );
                    }
                } );
                if ( field instanceof TextField )
                {
                    final TextField tf = ( TextField ) field;
                    tf.addTextChangeListener( new FieldEvents.TextChangeListener()
                    {
                        @Override
                        public void textChange( FieldEvents.TextChangeEvent event )
                        {
                            if ( form.isModified() || !event.getText().equals( tf.getValue() ) )
                            {
                                submit.setEnabled( true );

                                // Not needed after first event unless
                                // want to detect also changes back to
                                // unmodified value.
                                tf.removeTextChangeListener( this );

                                // Has to be reset because the
                                // removeListener() setting causes
                                // updating the field value from the
                                // server-side.
                                tf.setValue( event.getText() );
                            }
                        }
                    } );
                }
                field.setImmediate( true );

                return field;
            }
        } );

        Button cancel = new Button( "Cancel" );
        cancel.addClickListener( new Button.ClickListener()
        {
            public void buttonClick( Button.ClickEvent event )
            {
                form.discard();  // Not really necessary
                form.setVisible( false ); // and close it
                table.discard(); // Discards possible addItem()
                table.setEnabled( true );
                if ( table.getData() != null )
                {
                    beans.removeItem( table.getData() );
                }
                newBean.setEnabled( true );
            }
        } );

        layout.addComponent( table );
        layout.addComponent( form );

        form.getFooter().addComponent( submit );
        form.getFooter().addComponent( cancel );

        layout.setSpacing( true );

        vlayout.addComponent( layout );
        vlayout.addComponent( newBean );

        setCompositionRoot( vlayout );
    }
}
