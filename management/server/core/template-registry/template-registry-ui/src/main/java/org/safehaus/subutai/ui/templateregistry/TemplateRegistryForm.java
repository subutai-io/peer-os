/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.templateregistry;


import java.util.List;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.api.templateregistry.TemplateTree;
import org.safehaus.subutai.shared.protocol.Disposable;

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
public class TemplateRegistryForm extends CustomComponent implements Disposable {


    private final AgentManager agentManager;
    private final TemplateRegistryManager registryManager;
    private HierarchicalContainer container;
    private Tree templateTree;


    public TemplateRegistryForm( AgentManager agentManager, TemplateRegistryManager registryManager ) {
        setHeight( 100, Unit.PERCENTAGE );

        this.agentManager = agentManager;
        this.registryManager = registryManager;

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName( Runo.SPLITPANEL_SMALL );
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );

        container = new HierarchicalContainer();
        container.addContainerProperty( "value", Template.class, null );
        container.addContainerProperty( "icon", Resource.class, new ThemeResource( "img/lxc/physical.png" ) );

        templateTree = new Tree( "Templates" );
        templateTree.setContainerDataSource( container );
        templateTree.setItemIconPropertyId( "icon" );
        templateTree.setImmediate( true );
        templateTree.setItemDescriptionGenerator( new AbstractSelect.ItemDescriptionGenerator() {

            @Override
            public String generateDescription( Component source, Object itemId, Object propertyId ) {
                String description = "";

                Item item = templateTree.getItem( itemId );
                if ( item != null ) {

                    Template template = ( Template ) item.getItemProperty( "value" ).getValue();
                    if ( template != null ) {
                        description = "Name: " + template.getTemplateName() + "<br>" + "Parent: " + template
                                .getParentTemplateName() + "<br>" + "Arch: " + template.getLxcArch() + "<br>"
                                + "Utsname: " + template.getLxcUtsname() + "<br>" + "Cfg Path: " + template
                                .getSubutaiConfigPath();
                    }
                }

                return description;
            }
        } );

        templateTree.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                Item item = templateTree.getItem( event.getProperty().getValue() );

                if ( item != null ) {
                    Template template = ( Template ) item.getItemProperty( "value" ).getValue();

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

        TextArea packagesChanged = new TextArea( "Packages Changed" );
        packagesChanged.setValue( "+package4\n+package5\n-package6\n-package7" );

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


    private void fillTemplateTree() {
        container.removeAllItems();
        addChildren( registryManager.getTemplateTree(), Template.getMasterTemplate() );
    }


    private void addChildren( TemplateTree tree, Template currentTemplate ) {
        Item templateItem = container.addItem( currentTemplate.getTemplateName() );
        templateItem.getItemProperty( "value" ).setValue( currentTemplate );
        templateTree.setItemCaption( currentTemplate.getTemplateName(), currentTemplate.getTemplateName() );

        Template parent = tree.getParentTemplate( currentTemplate );
        if ( parent != null ) {
            container.setParent( currentTemplate.getTemplateName(), parent.getTemplateName() );
        }

        List<Template> children = tree.getChildrenTemplates( currentTemplate );
        if ( children == null || children.isEmpty() ) {
            container.setChildrenAllowed( currentTemplate.getTemplateName(), false );
        }
        else {
            container.setChildrenAllowed( currentTemplate.getTemplateName(), true );
            for ( Template child : children ) {

                addChildren( tree, child );
            }

            templateTree.expandItem( currentTemplate.getTemplateName() );
        }
    }


    public void dispose() {

    }
}
