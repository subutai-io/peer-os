package org.safehaus.subutai.core.peer.ui.forms;


import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.core.peer.ui.PeerManagerPortalModule;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;


public class PeerManageActionsComponent extends HorizontalLayout
{
    private Button positiveButton;
    private Button negativeButton;
    private Button managePolicyButton;
    private PeerInfo peer;
    private PeerManagerActionsListener listener;


    public interface PeerManagerActionsListener
    {
        public void OnPositiveButtonTrigger( PeerInfo peer );

        public void OnNegativeButtonTrigger( PeerInfo peer );
    }


    public PeerManageActionsComponent( final PeerManagerPortalModule module, PeerInfo p, PeerManagerActionsListener callback )
    {
        this.listener = callback;

        peer = p;
        positiveButton = new Button();
        negativeButton = new Button();
        managePolicyButton = new Button("Manage Policy");

        positiveButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                if ( listener != null )
                {
                    listener.OnPositiveButtonTrigger( peer );
                    updateView();
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
                    updateView();
                }
            }
        } );
        updateView();

        managePolicyButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                PolicyManagerWindow policyManagerWindow = new PolicyManagerWindow( module.getPeerManager(), peer );
                UI.getCurrent().addWindow( policyManagerWindow );
            }
        } );
    }


    //TODO update correct peer registration and further relationship management
    public void updateView()
    {
        removeComponent( positiveButton );
        removeComponent( negativeButton );
        removeComponent( managePolicyButton );

        switch ( peer.getStatus() )
        {
            case REQUESTED:
                positiveButton.setCaption( "Register" );
                negativeButton.setCaption( "Reject" );
                addComponent( positiveButton );
                addComponent( negativeButton );
                addComponent( managePolicyButton );
                break;
            case REGISTERED:
                positiveButton.setCaption( "Unregister" );
                addComponent( positiveButton );
                addComponent( managePolicyButton );
                break;
            case BLOCKED:
                positiveButton.setCaption( "Unlock" );
                negativeButton.setCaption( "Delete" );
                addComponent( positiveButton );
                addComponent( negativeButton );
                break;
            case BLOCKED_PEER:
                negativeButton.setCaption( "Delete" );
                addComponent( negativeButton );
                break;
            case REJECTED:
                negativeButton.setCaption( "Delete" );
                addComponent( negativeButton );
                break;
            case APPROVED:
                negativeButton.setCaption( "Unregister" );
                addComponent( negativeButton );
                addComponent( managePolicyButton );
                break;
            case REQUEST_SENT:
                negativeButton.setCaption( "Cancel request" );
                addComponent( negativeButton );
                addComponent( managePolicyButton );
                break;
        }
    }
}
