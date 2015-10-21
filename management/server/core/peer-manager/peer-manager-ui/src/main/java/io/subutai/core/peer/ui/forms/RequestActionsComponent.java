package io.subutai.core.peer.ui.forms;


import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;

import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.RegistrationRequest;
import io.subutai.core.peer.ui.PeerManagerPortalModule;


public class RequestActionsComponent extends HorizontalLayout
{
    private Button positiveButton;
    private Button negativeButton;
    private Button managePolicyButton;
    private RegistrationRequest registrationRequest;
    private RequestActionListener listener;


    public interface RequestActionListener
    {
        public void OnPositiveButtonTrigger( RegistrationRequest request,
                                             RequestUpdateViewListener updateViewListener );

        public void OnNegativeButtonTrigger( RegistrationRequest request,
                                             RequestUpdateViewListener updateViewListener );
    }


    public interface RequestUpdateViewListener
    {
        public void updateViewCallback();
    }


    public RequestActionsComponent( final PeerManagerPortalModule module, RegistrationRequest request,
                                    RequestActionListener callback )
    {
        this.listener = callback;
        this.registrationRequest = request;
        positiveButton = new Button();
        negativeButton = new Button();
        managePolicyButton = new Button( "Manage Policy" );

        positiveButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                if ( listener != null )
                {
                    listener.OnPositiveButtonTrigger( registrationRequest, new RequestUpdateViewListener()
                    {
                        @Override
                        public void updateViewCallback()
                        {
                            updateView();
                        }
                    } );
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
                    listener.OnNegativeButtonTrigger( registrationRequest, new RequestUpdateViewListener()
                    {
                        @Override
                        public void updateViewCallback()
                        {
                            updateView();
                        }
                    } );
                }
            }
        } );
        updateView();

        managePolicyButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                PolicyManagerWindow policyManagerWindow =
                        new PolicyManagerWindow( module.getPeerManager(), registrationRequest.getPeerInfo() );
                UI.getCurrent().addWindow( policyManagerWindow );
            }
        } );
    }


    //TODO update correct peer registration and further relationship management, also will need to check on server
    // side for relevance of incoming request
    public void updateView()
    {
        removeComponent( positiveButton );
        removeComponent( negativeButton );
        removeComponent( managePolicyButton );

        switch ( this.registrationRequest.getStatus() )
        {
            case REQUESTED:
                positiveButton.setCaption( "Approve" );
                negativeButton.setCaption( "Reject" );
                addComponent( positiveButton );
                addComponent( negativeButton );
                break;

            case APPROVED:
                positiveButton.setCaption( "Unregister" );
                addComponent( positiveButton );
//                addComponent( managePolicyButton );
                break;
//            case REGISTERED:
//                positiveButton.setCaption( "Unregister" );
//                addComponent( positiveButton );
//                addComponent( managePolicyButton );
//                break;
//            case BLOCKED:
//                positiveButton.setCaption( "Unlock" );
//                negativeButton.setCaption( "Delete" );
//                addComponent( positiveButton );
//                addComponent( negativeButton );
//                break;
//            case BLOCKED_PEER:
//                negativeButton.setCaption( "Delete" );
//                addComponent( negativeButton );
//                break;
//            case REJECTED:
//                negativeButton.setCaption( "Delete" );
//                addComponent( negativeButton );
//                break;
            case WAIT:
                negativeButton.setCaption( "Cancel" );
                addComponent( negativeButton );
                addComponent( managePolicyButton );
                break;
//            case REQUEST_SENT:
//                negativeButton.setCaption( "Cancel request" );
//                addComponent( negativeButton );
//                addComponent( managePolicyButton );
//                break;
        }
    }
}
