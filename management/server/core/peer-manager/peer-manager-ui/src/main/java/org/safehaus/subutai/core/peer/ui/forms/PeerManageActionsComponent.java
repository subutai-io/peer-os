package org.safehaus.subutai.core.peer.ui.forms;


import org.safehaus.subutai.core.peer.api.Peer;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;


/**
 * Created by talas on 10/14/14.
 */
public class PeerManageActionsComponent extends HorizontalLayout
{
    private Button positiveButton;
    private Button negativeButton;
    private Peer peer;
    private PeerManagerActionsListener listener;


    public interface PeerManagerActionsListener
    {
        public void OnPositiveButtonTrigger( Peer peer );

        public void OnNegativeButtonTrigger( Peer peer );
    }


    public PeerManageActionsComponent( Peer p, PeerManagerActionsListener callback )
    {
        this.listener = callback;

        peer = p;
        positiveButton = new Button();
        negativeButton = new Button();

        positiveButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                if ( listener != null )
                {
                    listener.OnPositiveButtonTrigger( peer );
                }
            }
        } );

        negativeButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                if ( listener != null )
                {
                    listener.OnNegativeButtonTrigger( peer );
                }
            }
        } );
        updateView();
    }


    public void updateView()
    {
        removeComponent( positiveButton );
        removeComponent( negativeButton );

        switch ( peer.getStatus() )
        {
            case REQUESTED:
                positiveButton.setCaption( "Register" );
                negativeButton.setCaption( "Reject" );
                addComponent( positiveButton );
                addComponent( negativeButton );
                break;
            case REGISTERED:
                positiveButton.setCaption( "Unregister" );
                addComponent( positiveButton );
                break;
            case BLOCKED:
                positiveButton.setCaption( "Unlock" );
                negativeButton.setCaption( "Delete" );
                addComponent( positiveButton );
                addComponent( negativeButton );
                break;
            case REJECTED:
                negativeButton.setCaption( "Delete" );
                addComponent( negativeButton );
                break;
        }
    }
}
