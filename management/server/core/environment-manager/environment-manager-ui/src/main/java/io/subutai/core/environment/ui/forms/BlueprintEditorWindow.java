package io.subutai.core.environment.ui.forms;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.ContainerType;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.network.Gateway;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.Template;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.peer.api.PeerManager;


public class BlueprintEditorWindow extends Window
{
    private static final Logger LOG = LoggerFactory.getLogger( BlueprintEditorWindow.class );

    private final Blueprint blueprint;
    private final PeerManager peerManager;
    private final TextField nameTxt;
    private Table placementTable;
    private Button saveBtn;


    public BlueprintEditorWindow( final Blueprint blueprint, final PeerManager peerManager,
                                  final EnvironmentManager environmentManager )
    {

        this.blueprint = blueprint;
        this.peerManager = peerManager;

        setCaption( "Blueprint Editor" );
        setWidth( "800px" );
        setHeight( "600px" );
        setModal( true );
        setClosable( true );

        VerticalLayout content = new VerticalLayout();
        content.setSpacing( true );
        content.setMargin( true );
        content.setStyleName( "default" );
        content.setSizeFull();


        Table nodeGroupsTable = createNodeGroupsTable();

        populateNodeGroupsTable( nodeGroupsTable );

        placementTable = createPlacementTable();

        content.addComponent( nodeGroupsTable );

        content.addComponent( placementTable );

        saveBtn = new Button( "Save" );
        saveBtn.setId( "saveButton" );
        saveBtn.setEnabled( true );
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                //buildProcessTrigger( environmentManager, grow );
                final Blueprint blueprint1 = getBlueprint();
                LOG.info( JsonUtil.toJson( blueprint1 ) );
                try
                {
                    environmentManager.saveBlueprint( blueprint1 );
                    Notification.show( "Blueprint saved successfully." );
                }
                catch ( EnvironmentManagerException e )
                {
                    Notification.show( "Error occured on saving blueprint.", Notification.Type.ERROR_MESSAGE );
                }
            }
        } );

        Button closeBtn = new Button( "Close" );
        closeBtn.setId( "closeButton" );
        closeBtn.setEnabled( true );
        closeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                close();
            }
        } );


        nameTxt = new TextField( "Blueprint name" );
        nameTxt.setId( "nameTxt" );
        nameTxt.setImmediate( true );
        nameTxt.setValue( blueprint.getName() );
        content.addComponent( nameTxt );

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing( true );
        buttons.addComponent( saveBtn );
        buttons.addComponent( closeBtn );

        content.addComponent( buttons );

        content.setComponentAlignment( buttons, Alignment.TOP_RIGHT );

        initNodeGroupsTable();

        setContent( content );
    }


    private Blueprint getBlueprint()
    {
        Set<NodeGroup> nodeGroups = new HashSet();

        for ( Object itemId : placementTable.getItemIds() )
        {
            Item item = placementTable.getItem( itemId );
            int amount = Integer.parseInt( item.getItemProperty( "Amount" ).getValue().toString() );
            String nodeGroupName = item.getItemProperty( "Name" ).getValue().toString();
            String templateName = item.getItemProperty( "Template" ).getValue().toString();
            ContainerType type = ( ContainerType ) item.getItemProperty( "Type" ).getValue();
            NodeGroup nodeGroup = new NodeGroup( nodeGroupName, templateName, type, amount, 0, 0 );
            nodeGroups.add( nodeGroup );
        }

        final Blueprint b = new Blueprint( nameTxt.getValue(), nodeGroups );

        b.setId( this.blueprint.getId() );
        b.setContainerDistributionType( this.blueprint.getContainerDistributionType() );
        return b;
    }


    private Table createPlacementTable()
    {
        final Table table = new Table();
        table.addContainerProperty( "Template", Template.class, null );
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "Amount", Integer.class, null );
        table.addContainerProperty( "Type", ContainerType.class, null );
        table.addContainerProperty( "Remove", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();

        return table;
    }


    private void populateNodeGroupsTable( final Table nodeGroupsTable )
    {
        //        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() )
        //        {
        TextField nodeGroupNameField = new TextField( null, "Sample node group" );
        Slider slider = new Slider( 1, 10 );
        slider.setWidth( 100, Unit.PIXELS );
        slider.setOrientation( SliderOrientation.HORIZONTAL );
        slider.setValue( ( double ) 1 );

        ComboBox typesCombo = createTypesComboBox();
        typesCombo.setId( "typesCombo" );

        ComboBox templatesCombo = createTemplatesComboBox();
        typesCombo.setId( "templatesCombo" );

        Button placeBtn = createPlaceButton( templatesCombo, nodeGroupNameField, slider, typesCombo );

        nodeGroupsTable.addItem( new Object[] {
                templatesCombo, nodeGroupNameField, slider, typesCombo, placeBtn
        }, null );
        //        }
    }


    private Button createPlaceButton( final ComboBox templatesCombo, final TextField nodeGroup, final Slider slider,
                                      final ComboBox type )
    {
        Button placeButton = new Button( "Place" );
        placeButton.setId( "placeButton" );

        placeButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {

                if ( templatesCombo.getValue() == null )
                {
                    Notification.show( "Select template to place", Notification.Type.WARNING_MESSAGE );
                }
                else if ( type.getValue() == null )
                {
                    Notification.show( "Select container type to place", Notification.Type.WARNING_MESSAGE );
                }
                else if ( nodeGroup.getValue() == null )
                {
                    Notification.show( "Select unique node group name to place", Notification.Type.WARNING_MESSAGE );
                }
                else if ( nodeGroup.getValue() == null )
                {
                    Notification.show( "Select unique node group name to place", Notification.Type.WARNING_MESSAGE );
                }
                else if ( slider.getValue().intValue() == 0 )
                {
                    Notification.show( "Select number of nodes to place", Notification.Type.WARNING_MESSAGE );
                }
                else if ( placementTable.getItem( nodeGroup.getValue().trim() ) != null )
                {
                    Notification.show( "Node group already exists. Please change name of node group.",
                            Notification.Type.WARNING_MESSAGE );
                }
                else
                {
                    placeNodeGroup( templatesCombo, nodeGroup, slider, type );
                }
            }
        } );

        return placeButton;
    }


    private void placeNodeGroup( final ComboBox templatesCombo, TextField nodeGroupNameField, final Slider slider,
                                 ComboBox type )
    {
        final String nodeGroupName = nodeGroupNameField.getValue().trim();

        final String rowId = String.format( "%s", nodeGroupName );

        Button removeBtn = new Button( "Remove" );
        removeBtn.setId( "removeButton" );


        final int amount = slider.getValue().intValue();
        removeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                placementTable.removeItem( rowId );
            }
        } );

        final Item row = placementTable.getItem( rowId );

        if ( row == null )
        {
            placementTable.addItem( new Object[] {
                    templatesCombo.getValue(), nodeGroupName, amount, type.getValue(), removeBtn
            }, rowId );
        }
    }


    private void initNodeGroupsTable()
    {
        for ( NodeGroup nodeGroup : this.blueprint.getNodeGroups() )
        {
            final String nodeGroupName = nodeGroup.getName();
            int amount = nodeGroup.getNumberOfContainers();
            ContainerType type = nodeGroup.getType();
            final String rowId = String.format( "%s", nodeGroupName );


            Button removeBtn = new Button( "Remove" );
            removeBtn.setId( "removeButton-" + rowId );


            removeBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent event )
                {
                    placementTable.removeItem( rowId );
                }
            } );


            placementTable.addItem( new Object[] {
                    peerManager.getLocalPeer().getTemplateByName( nodeGroup.getTemplateName() ), nodeGroupName, amount,
                    type, removeBtn
            }, rowId );
        }
    }


    private ComboBox createTypesComboBox()
    {
        ComboBox typesCombo = new ComboBox();
        typesCombo.setNullSelectionAllowed( false );
        typesCombo.setTextInputAllowed( false );
        typesCombo.setImmediate( true );
        typesCombo.setRequired( true );

        for ( ContainerType t : ContainerType.values() )
        {
            typesCombo.addItem( t );
        }

        return typesCombo;
    }


    private ComboBox createTemplatesComboBox()
    {
        ComboBox typesCombo = new ComboBox();
        typesCombo.setNullSelectionAllowed( false );
        typesCombo.setTextInputAllowed( false );
        typesCombo.setImmediate( true );
        typesCombo.setRequired( true );

        for ( Template t : peerManager.getLocalPeer().getTemplates() )
        {
            typesCombo.addItem( t );
        }

        return typesCombo;
    }


    private Table createNodeGroupsTable()
    {
        Table table = new Table();
        table.addContainerProperty( "Template", ComboBox.class, null );
        table.addContainerProperty( "Name", TextField.class, null );
        table.addContainerProperty( "Amount", Slider.class, null );
        //        table.addContainerProperty( "Host", ComboBox.class, null );
        table.addContainerProperty( "Type", ComboBox.class, null );
        table.addContainerProperty( "Action", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }
}
