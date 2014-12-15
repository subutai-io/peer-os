/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.registry.ui;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.git.api.GitChangedFile;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.registry.api.TemplateTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 *
 */
public class TemplateRegistryComponent extends CustomComponent
{

    private static final String VALUE_PROPERTY = "value";
    private static final String PHYSICAL_IMG = "img/lxc/physical.png";
    private static final String ICON = "icon";
    private final TemplateRegistry registryManager;
    private HierarchicalContainer container;
    private Tree templateTree;

    private GridLayout grid;
    private Table templateInfoTable;
    private Table changedFilesTable;
    private TextArea packagesInstalled;
    private TextArea packagesChanged;

    private static final String TEMPLATE_PROPERTY = "Template Property";
    private static final String TEMPLATE_VALUE = "Value";
    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateRegistryComponent.class );


    private interface TemplateValue
    {
        public String getTemplateProperty( Template template );
    }


    private Map<String, TemplateValue> templatePropertiesMap = new HashMap<String, TemplateValue>()
    {
        {
            put( "Template Name", new TemplateValue()
            {
                @Override
                public String getTemplateProperty( final Template template )
                {
                    return template.getTemplateName();
                }
            } );
            put( "Parent Name", new TemplateValue()
            {
                @Override
                public String getTemplateProperty( final Template template )
                {
                    return template.getParentTemplateName();
                }
            } );
            put( "Lxc Arch", new TemplateValue()
            {
                @Override
                public String getTemplateProperty( final Template template )
                {
                    return template.getLxcArch();
                }
            } );
            put( "Utsname", new TemplateValue()
            {
                @Override
                public String getTemplateProperty( final Template template )
                {
                    return template.getLxcUtsname();
                }
            } );
            put( "Config Path", new TemplateValue()
            {
                @Override
                public String getTemplateProperty( final Template template )
                {
                    return template.getSubutaiConfigPath();
                }
            } );
            //            put( "App Data Path", new TemplateValue()
            //            {
            //                @Override
            //                public String getTemplateProperty( final Template template )
            //                {
            //                    return template.getPat;
            //                }
            //            } );
        }
    };


    public TemplateRegistryComponent( final TemplateRegistry registryManager )
    {
        setHeight( 100, Unit.PERCENTAGE );

        this.registryManager = registryManager;

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName( Runo.SPLITPANEL_SMALL );
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );

        container = new HierarchicalContainer();
        container.addContainerProperty( VALUE_PROPERTY, Template.class, null );
        container.addContainerProperty( ICON, Resource.class, new ThemeResource( PHYSICAL_IMG ) );

        templateTree = new Tree( "Templates" );
        templateTree.setContainerDataSource( container );
        templateTree.setItemIconPropertyId( ICON );
        templateTree.setImmediate( true );
        templateTree.setItemDescriptionGenerator( new AbstractSelect.ItemDescriptionGenerator()
        {
            @Override
            public String generateDescription( Component source, Object itemId, Object propertyId )
            {
                String description = "";
                Item item = templateTree.getItem( itemId );
                if ( item != null )
                {
                    Template template = ( Template ) item.getItemProperty( VALUE_PROPERTY ).getValue();
                    if ( template != null )
                    {
                        description = getTemplateDescription( template );
                    }
                }
                return description;
            }
        } );

        templateTree.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                Item item = templateTree.getItem( event.getProperty().getValue() );

                if ( item != null )
                {
                    final Template template = ( Template ) item.getItemProperty( VALUE_PROPERTY ).getValue();
                    if ( template != null )
                    {
                        Notification.show( template.toString() );
                        getUI().access( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                showSelectedTemplateInfo( template );
                            }
                        } );
                        try
                        {
                            Container dataContainer = new BeanItemContainer<>( GitChangedFile.class );
                            List<GitChangedFile> changedFiles = registryManager.getChangedFiles( template );
                            for ( final GitChangedFile changedFile : changedFiles )
                            {
                                dataContainer.addItem( changedFile );
                            }
                            changedFilesTable.setContainerDataSource( dataContainer );
                            //TODO instead of listing all changed files in TextArea list all of them in Table
                            //where all files will be shown with appropriate columns and implement user
                            //table row click listener, after that need to implement functionality to show diffs
                            //of changed files by different colouring stuff
                        }
                        catch ( RegistryException e )
                        {
                            LOGGER.error( "Error getting changed files from repo.", e );
                        }
                    }
                    else
                    {
                        Notification.show( "No template" );
                    }
                }
            }
        } );

        fillTemplateTree();

        horizontalSplit.setFirstComponent( templateTree );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        changedFilesTable = new Table( "Changed Files." );
        changedFilesTable.setWidth( "21%" );
        changedFilesTable.setImmediate( true );

        templateInfoTable = new Table( "Template Info" );
        templateInfoTable.setWidth( "25%" );
        templateInfoTable.setImmediate( true );
        templateInfoTable.addContainerProperty( TEMPLATE_PROPERTY, String.class, null );
        templateInfoTable.addContainerProperty( TEMPLATE_VALUE, String.class, null );

        for ( String key : templatePropertiesMap.keySet() )
        {
            templateInfoTable.addItem( new Object[] { key, "" }, key );
        }

        verticalLayout.addComponent( templateInfoTable );
        verticalLayout.addComponent( changedFilesTable );

        packagesInstalled = new TextArea( "Packages Installed" );
        packagesInstalled.setValue( "" );
        packagesInstalled.setReadOnly( true );

        packagesChanged = new TextArea( "Packages Changed" );
        packagesChanged.setValue( "" );
        packagesChanged.setReadOnly( true );

        HorizontalLayout packagesLayout = new HorizontalLayout();
        packagesLayout.addComponent( packagesInstalled );
        packagesLayout.addComponent( packagesChanged );

        verticalLayout.addComponent( packagesLayout );


        Label confirmationLbl = new Label( "<font style='color:red'>some lines which were deleted</font><br/>"
                + "<font style='color:green'>some lines which were added</font><br/>" );
        confirmationLbl.setContentMode( ContentMode.HTML );

        verticalLayout.addComponent( confirmationLbl );

        horizontalSplit.setSecondComponent( verticalLayout );
        setCompositionRoot( horizontalSplit );
    }


    private void showSelectedTemplateInfo( Template template )
    {
        for ( String key : templatePropertiesMap.keySet() )
        {
            Property item = templateInfoTable.getItem( key ).getItemProperty( TEMPLATE_VALUE );
            String templateValue = ( String ) item.getValue();
            templateValue = templatePropertiesMap.get( key ).getTemplateProperty( template );
            //            item.setValue( templatePropertiesMap.get( key ).getTemplateProperty( template ) );
        }
        StringBuilder products = new StringBuilder();
        for ( String product : template.getProducts() )
        {
            product = product + "\n";
            products.append( product );
        }
        packagesInstalled.setReadOnly( false );
        packagesInstalled.setValue( products.toString() );
        packagesInstalled.setReadOnly( true );

        StringBuilder packages = new StringBuilder();
        Set<String> diff = registryManager.getPackagesDiff( template );
        for ( String templatePackage : diff )
        {
            packages.append( templatePackage ).append( "\n" );
        }
        packagesChanged.setReadOnly( false );
        packagesChanged.setValue( packages.toString() );
        packagesChanged.setReadOnly( true );
    }


    private String getTemplateDescription( Template template )
    {
        if ( template != null )
        {
            return "Name: " + template.getTemplateName() + "<br>" + "Parent: " + template.getParentTemplateName()
                    + "<br>" + "Arch: " + template.getLxcArch() + "<br>" + "Utsname: " + template.getLxcUtsname()
                    + "<br>" + "Cfg Path: " + template.getSubutaiConfigPath();
        }
        return "";
    }


    private void fillTemplateTree()
    {
        container.removeAllItems();
        TemplateTree tree = registryManager.getTemplateTree();
        List<Template> rootTemplates = tree.getRootTemplates();
        if ( rootTemplates != null )
        {
            for ( Template template : rootTemplates )
            {
                addChildren( tree, template );
            }
        }
    }


    private void addChildren( TemplateTree tree, Template currentTemplate )
    {
        String itemId = String.format( "%s-%s", currentTemplate.getTemplateName(), currentTemplate.getLxcArch() );
        Item templateItem = container.addItem( itemId );
        templateItem.getItemProperty( VALUE_PROPERTY ).setValue( currentTemplate );
        templateTree.setItemCaption( itemId, currentTemplate.getTemplateName() );

        Template parent = tree.getParentTemplate( currentTemplate );
        if ( parent != null )
        {
            container.setParent( itemId, String.format( "%s-%s", parent.getTemplateName(), parent.getLxcArch() ) );
        }

        List<Template> children = tree.getChildrenTemplates( currentTemplate );
        if ( children == null || children.isEmpty() )
        {
            container.setChildrenAllowed( itemId, false );
        }
        else
        {
            container.setChildrenAllowed( itemId, true );
            for ( Template child : children )
            {

                addChildren( tree, child );
            }

            templateTree.expandItem( itemId );
        }
    }
}
