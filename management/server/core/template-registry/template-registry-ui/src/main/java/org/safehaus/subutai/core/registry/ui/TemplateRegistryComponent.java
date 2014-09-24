/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.registry.ui;


import java.util.List;

import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;
import org.safehaus.subutai.core.registry.api.TemplateTree;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
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
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
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
    private final TemplateRegistryManager registryManager;
    private HierarchicalContainer container;
    private Tree templateTree;


    public TemplateRegistryComponent( TemplateRegistryManager registryManager )
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
                    Template template = ( Template ) item.getItemProperty( VALUE_PROPERTY ).getValue();

                    Notification.show( template.toString() );
                }
            }
        } );

        fillTemplateTree();

        horizontalSplit.setFirstComponent( templateTree );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        GridLayout grid = new GridLayout( 4, 4 );

        TextField templateNameTxt = new TextField( "Template name" );
        templateNameTxt.setReadOnly( true );
        grid.addComponent( templateNameTxt, 0, 0 );
        TextField templateParentTxt = new TextField( "Parent name" );
        templateParentTxt.setReadOnly( true );
        grid.addComponent( templateParentTxt, 1, 0 );
        TextField lxcArchTxt = new TextField( "Lxc arch" );
        lxcArchTxt.setReadOnly( true );
        grid.addComponent( lxcArchTxt, 2, 0 );
        TextField lxcUtsnameTxt = new TextField( "Utsname" );
        lxcUtsnameTxt.setReadOnly( true );
        grid.addComponent( lxcUtsnameTxt, 0, 1 );
        TextField cfgPathTxt = new TextField( "Config path" );
        cfgPathTxt.setReadOnly( true );
        grid.addComponent( cfgPathTxt, 1, 1 );
        TextField appDataPathTxt = new TextField( "App Data path" );
        appDataPathTxt.setReadOnly( true );
        grid.addComponent( appDataPathTxt, 2, 1 );
        verticalLayout.addComponent( grid );

        TextArea packagesInstalled = new TextArea( "Packages Installed" );
        packagesInstalled.setValue( "package1\npackage2\npackage3" );
        packagesInstalled.setReadOnly( true );

        TextArea packagesChanged = new TextArea( "Packages Changed" );
        packagesChanged.setValue( "+package4\n+package5\n-package6\n-package7" );
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
