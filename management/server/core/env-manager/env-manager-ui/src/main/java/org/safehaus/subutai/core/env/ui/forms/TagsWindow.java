package org.safehaus.subutai.core.env.ui.forms;


import org.safehaus.subutai.common.peer.ContainerHost;

import com.google.common.base.Strings;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
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
        //        tagsSelect.setReadOnly( true );
        tagsSelect.setRows( 5 );
        tagsSelect.setWidth( "100px" );
        tagsSelect.setMultiSelect( false );

        content.addComponent( tagsSelect );

        final TextField newTagTxt = new TextField( "New tag" );

        content.addComponent( newTagTxt );

        Button addNewTagBtn = new Button( "Add" );

        addNewTagBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {

                if ( !Strings.isNullOrEmpty( newTagTxt.getValue() ) )
                {
                    containerHost.addTag( newTagTxt.getValue().trim() );
                    tagsSelect.setContainerDataSource( new IndexedContainer( containerHost.getTags() ) );
                }
                else
                {
                    Notification.show( "Please, enter valid tag" );
                }
            }
        } );

        content.addComponent( addNewTagBtn );

        setContent( content );
    }
}
