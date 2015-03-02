package org.safehaus.subutai.core.env.ui.forms;


import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.LocalPeer;

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


    public TagsWindow( final ContainerHost containerHost, LocalPeer localPeer )
    {
        ContainerHost localContainer = null;
        try
        {
            localContainer = localPeer.getContainerHostById( containerHost.getId() );
        }
        catch ( HostNotFoundException e )
        {
            //ignore, this is a remote container
        }

        setCaption( "Tags" );
        setWidth( "500px" );
        setHeight( "300px" );
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

        final ContainerHost finalLocalContainer = localContainer;
        addTagBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {

                if ( !Strings.isNullOrEmpty( tagTxt.getValue() ) )
                {
                    String tag = tagTxt.getValue().trim();
                    containerHost.addTag( tag );
                    tagsSelect.setContainerDataSource( new IndexedContainer( containerHost.getTags() ) );
                    if ( finalLocalContainer != null )
                    {
                        finalLocalContainer.addTag( tag );
                    }
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
                    String tag = String.valueOf( tagsSelect.getValue() ).trim();
                    containerHost.removeTag( tag );
                    tagsSelect.setContainerDataSource( new IndexedContainer( containerHost.getTags() ) );
                    if ( finalLocalContainer != null )
                    {
                        finalLocalContainer.removeTag( tag );
                    }
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
