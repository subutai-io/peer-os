package org.safehaus.subutai.core.environment.ui.wizard;


import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.topology.Blueprint2PeerGroupData;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.peer.api.PeerGroup;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * Created by bahadyr on 9/10/14.
 */
public class Blueprint2PeerGroupWizard extends Window
{

    private int step = 0;
    private EnvironmentManagerPortalModule module;
    private EnvironmentBlueprint blueprint;


    public Blueprint2PeerGroupWizard( final String caption, EnvironmentManagerPortalModule module,
                                      EnvironmentBlueprint blueprint )
    {
        super( caption );
        setCaption( caption );
        setModal( true );
        setClosable( true );
        setVisible( false );
        setWidth( "800px" );
        setHeight( "200px" );
        this.module = module;
        this.blueprint = blueprint;
        next();
    }


    public void next()
    {
        step++;
        putForm();
    }


    private void putForm()
    {
        switch ( step )
        {
            case 1:
            {
                setContent( generatePeerGroupsLayout() );
                break;
            }
            case 2:
            {
                close();
                break;
            }
            default:
            {
                close();
                break;
            }
        }
    }


    public EnvironmentManagerPortalModule getModule()
    {
        return module;
    }


    public void setModule( final EnvironmentManagerPortalModule module )
    {
        this.module = module;
    }


    public void back()
    {
        step--;
    }


    final ComboBox peerGroupsCombo = new ComboBox();


    private VerticalLayout generatePeerGroupsLayout()
    {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin( true );

        List<PeerGroup> peerGroups = module.getPeerManager().peersGroups();

        BeanItemContainer<PeerGroup> bic = new BeanItemContainer<>( PeerGroup.class );
        bic.addAll( peerGroups );

        peerGroupsCombo.setContainerDataSource( bic );
        peerGroupsCombo.setNullSelectionAllowed( false );
        peerGroupsCombo.setTextInputAllowed( false );
        peerGroupsCombo.setItemCaptionPropertyId( "name" );

        Button nextButton = new Button( "Save build task" );
        nextButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                PeerGroup peerGroup = getSelectedPeerGroup();
                if ( peerGroup != null )
                {
                    Blueprint2PeerGroupData data = new Blueprint2PeerGroupData( blueprint.getId(), peerGroup.getId()  );
                    try
                    {
                        module.getEnvironmentManager().saveBuildProcess( data );
                    }
                    catch ( EnvironmentManagerException e )
                    {
                        Notification.show( e.getMessage() );
                    }
                    next();
                }
                else
                {
                    Notification.show( "Please select peer group", Notification.Type.HUMANIZED_MESSAGE );
                }
            }
        } );


        vl.addComponent( peerGroupsCombo );
        vl.addComponent( nextButton );
        return vl;
    }


    private PeerGroup getSelectedPeerGroup()
    {
        return ( PeerGroup ) peerGroupsCombo.getValue();
    }
}
