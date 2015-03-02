package org.safehaus.subutai.core.env.ui.forms;


import org.safehaus.subutai.common.peer.ContainerHost;

import com.google.common.base.Strings;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class TagsWindow extends Window
{

    private final ContainerHost containerHost;


    public TagsWindow( final ContainerHost containerHost )
    {
        this.containerHost = containerHost;

        setCaption( "Tags" );
        setWidth( "600px" );
        setHeight( "400px" );
        setModal( true );
        setClosable( true );


        VerticalLayout content = new VerticalLayout();
        content.setSpacing( true );
        content.setMargin( true );
        content.setStyleName( "default" );
        content.setSizeFull();


        final ListSelect tagsSelect = new ListSelect( "Tags", containerHost.getTags() );
        tagsSelect.setNullSelectionAllowed( false );
        tagsSelect.setRows( 5 );
        tagsSelect.setWidth( "100px" );
        tagsSelect.setMultiSelect( false );

        content.addComponent( tagsSelect );

        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing( true );

        content.addComponent( controls );


        Label tagLbl = new Label( "Enter tag" );

        controls.addComponent( tagLbl );

        final TextField tagTxt = new TextField();

        controls.addComponent( tagTxt );

        Button addTagBtn = new Button( "Add" );

        addTagBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {

                if ( !Strings.isNullOrEmpty( tagTxt.getValue() ) )
                {
                    containerHost.addTag( tagTxt.getValue().trim() );
                    tagsSelect.setContainerDataSource( new IndexedContainer( containerHost.getTags() ) );
                }
                else
                {
                    Notification.show( "Please, enter valid tag" );
                }
            }
        } );

        controls.addComponent( addTagBtn );

        Button removeTagBtn = new Button( "Remove" );

        removeTagBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                if ( tagsSelect.getValue() != null )
                {
                    containerHost.removeTag( String.valueOf( tagsSelect.getValue() ) );
                    tagsSelect.setContainerDataSource( new IndexedContainer( containerHost.getTags() ) );
                }
                else
                {
                    Notification.show( "Please, enter valid tag" );
                }
            }
        } );

        controls.addComponent( removeTagBtn );

        setContent( content );
    }
}
