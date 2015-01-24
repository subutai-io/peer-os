package org.safehaus.subutai.core.env.ui.forms;


import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.api.build.Blueprint;
import org.safehaus.subutai.core.env.api.build.NodeGroup;
import org.safehaus.subutai.core.env.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.env.ui.EnvironmentManagerComponent;

import com.google.common.collect.Sets;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;


public class BlueprintForm
{
    private static final String BLUEPRINT = "Blueprint";
    private static final String SAVE = "Save";
    private static final String PLEASE_PROVIDE_A_BLUEPRINT = "Please provide a blueprint";
    private static final String ERROR_SAVING_BLUEPRINT = "Error saving blueprint";
    private static final String BLUEPRINT_SAVED = "Blueprint saved";
    private static final String VIEW_BLUEPRINTS = "View blueprints";
    private static final String NAME = "Name";
    private static final String BUILD = "Build";
    private static final String VIEW = "View";
    private static final String DELETE = "Delete";


    private final VerticalLayout contentRoot;
    private final EnvironmentManager environmentManager;
    private final EnvironmentManagerComponent environmentManagerComponent;
    private TextArea blueprintTxtArea;
    private Table blueprintsTable;
    private Button viewBlueprintsButton;


    public BlueprintForm( EnvironmentManagerComponent environmentManagerComponent,
                          EnvironmentManager environmentManager )
    {
        this.environmentManagerComponent = environmentManagerComponent;
        this.environmentManager = environmentManager;
        contentRoot = new VerticalLayout();

        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        blueprintTxtArea = getBlueprintTxtArea();

        Button loadBlueprintButton = new Button( SAVE );
        loadBlueprintButton.setId( "loadBlueprintButton" );

        loadBlueprintButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                uploadAndSaveBlueprint();
            }
        } );

        Button putSampleBlueprint = new Button( "Get sample" );
        putSampleBlueprint.setId( "putSampleBlueprint" );
        putSampleBlueprint.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {


                String blueprintStr = JsonUtil.toJson( getSampleBlueprint() );
                blueprintTxtArea.setValue( blueprintStr );
            }
        } );

        contentRoot.addComponent( blueprintTxtArea );
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing( true );
        buttons.addComponent( loadBlueprintButton );
        buttons.addComponent( putSampleBlueprint );
        contentRoot.addComponent( buttons );

        blueprintsTable = createBlueprintsTable( "Blueprints" );
        blueprintsTable.setId( "blueprintsTable" );

        viewBlueprintsButton = new Button( VIEW_BLUEPRINTS );
        viewBlueprintsButton.setId( "viewBlueprintsButton" );
        viewBlueprintsButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                updateBlueprintsTable();
            }
        } );

        contentRoot.addComponent( viewBlueprintsButton );
        contentRoot.addComponent( blueprintsTable );
    }


    private void updateBlueprintsTable()
    {
        blueprintsTable.removeAllItems();
        try
        {
            for ( final Blueprint blueprint : environmentManager.getBlueprints() )
            {
                final Button view = new Button( VIEW );
                view.setId( blueprint.getName() + "-view" );
                view.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        blueprintTxtArea.setValue( JsonUtil.toJson( blueprint ) );
                    }
                } );
                final Button delete = new Button( DELETE );
                delete.setId( blueprint.getName() + "-delete" );
                delete.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        try
                        {
                            environmentManager.removeBlueprint( blueprint.getId() );
                            updateBlueprintsTable();
                        }
                        catch ( EnvironmentManagerException e )
                        {
                            Notification.show( "Error removing blueprint", e.getMessage(),
                                    Notification.Type.ERROR_MESSAGE );
                        }
                    }
                } );
                final Button build = new Button( BUILD );
                build.setId( blueprint.getName() + "-build" );
                build.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        buildBlueprint( blueprint );
                    }
                } );

                blueprintsTable.addItem( new Object[] {
                        blueprint.getName(), view, delete, build
                }, null );
            }
        }
        catch ( EnvironmentManagerException e )
        {
            Notification.show( "Error loading blueprints", e.getMessage(), Notification.Type.ERROR_MESSAGE );
        }
        blueprintsTable.refreshRowCache();
    }


    private void buildBlueprint( Blueprint blueprint )
    {
        //TODO let user specify topology
        //TODO create environment in background thread
        environmentManagerComponent.focusEnvironmentForm();
    }


    private Table createBlueprintsTable( String caption )
    {
        Table table = new Table( caption );
        table.addContainerProperty( NAME, String.class, null );
        table.addContainerProperty( VIEW, Button.class, null );
        table.addContainerProperty( DELETE, Button.class, null );
        table.addContainerProperty( BUILD, Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }


    private Blueprint getSampleBlueprint()
    {
        NodeGroup nodeGroup = new NodeGroup( "Sample node group", "master", Common.DEFAULT_DOMAIN_NAME, 2, 0, 0,
                new PlacementStrategy( "ROUND_ROBIN" ) );
        return new Blueprint( "Sample blueprint", Sets.newHashSet( nodeGroup ) );
    }


    private TextArea getBlueprintTxtArea()
    {
        blueprintTxtArea = new TextArea( BLUEPRINT );
        blueprintTxtArea.setId( "blueprintTxtArea" );
        blueprintTxtArea.setSizeFull();
        blueprintTxtArea.setRows( 20 );
        blueprintTxtArea.setImmediate( true );
        blueprintTxtArea.setWordwrap( false );
        return blueprintTxtArea;
    }


    private void uploadAndSaveBlueprint()
    {
        String content = blueprintTxtArea.getValue().trim();
        if ( content.length() > 0 )
        {
            try
            {
                Blueprint blueprint = JsonUtil.fromJson( content, Blueprint.class );
                environmentManager.saveBlueprint( blueprint );
                updateBlueprintsTable();
                Notification.show( BLUEPRINT_SAVED );
            }
            catch ( Exception e )
            {
                Notification.show( ERROR_SAVING_BLUEPRINT, e.getMessage(), Notification.Type.ERROR_MESSAGE );
            }
        }
        else
        {
            Notification.show( PLEASE_PROVIDE_A_BLUEPRINT );
        }
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
