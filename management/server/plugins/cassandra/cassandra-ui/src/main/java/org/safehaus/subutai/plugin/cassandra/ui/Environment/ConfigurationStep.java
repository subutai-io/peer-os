package org.safehaus.subutai.plugin.cassandra.ui.Environment;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


public class ConfigurationStep extends VerticalLayout
{

    public ConfigurationStep( final EnvironmentWizard environmentWizard )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 3 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        final TextField clusterNameTxtFld = new TextField( "Enter cluster name" );
        clusterNameTxtFld.setInputPrompt( "Cluster name" );
        clusterNameTxtFld.setRequired( true );
        clusterNameTxtFld.setValue( environmentWizard.getConfig().getClusterName() );
        clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                environmentWizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField domainNameTxtFld = new TextField( "Enter domain name" );
        domainNameTxtFld.setInputPrompt( "Domain name" );
        domainNameTxtFld.setInputPrompt( "intra.lan" );
        domainNameTxtFld.setRequired( true );
        domainNameTxtFld.setValue( environmentWizard.getConfig().getClusterName() );
        domainNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                environmentWizard.getConfig().setDomainName( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField dataDirectoryTxtFld = new TextField( "Data directory" );
        dataDirectoryTxtFld.setInputPrompt( "/var/lib/cassandra/data" );
        dataDirectoryTxtFld.setRequired( true );
        dataDirectoryTxtFld.setValue( environmentWizard.getConfig().getClusterName() );
        dataDirectoryTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                environmentWizard.getConfig().setDataDirectory( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField commitLogDirectoryTxtFld = new TextField( "Commit log directory" );
        commitLogDirectoryTxtFld.setInputPrompt( "/var/lib/cassandra/commitlog" );
        commitLogDirectoryTxtFld.setRequired( true );
        commitLogDirectoryTxtFld.setValue( environmentWizard.getConfig().getClusterName() );
        commitLogDirectoryTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                environmentWizard.getConfig().setCommitLogDirectory( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField savedCachesDirectoryTxtFld = new TextField( "Saved caches directory" );
        savedCachesDirectoryTxtFld.setInputPrompt( "/var/lib/cassandra/saved_caches" );
        savedCachesDirectoryTxtFld.setRequired( true );
        savedCachesDirectoryTxtFld.setValue( environmentWizard.getConfig().getClusterName() );
        savedCachesDirectoryTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                environmentWizard.getConfig()
                                 .setSavedCachesDirectory( event.getProperty().getValue().toString().trim() );
            }
        } );

        List<Environment> environmentList = environmentWizard.getEnvironmentManager().getEnvironments();
        List<Environment> envList = new ArrayList<>();
        for ( Environment anEnvironmentList : environmentList )
        {
            boolean exists = isTemplateExists( anEnvironmentList.getContainerHosts(), "cassandra" );
            if ( exists )
            {
                envList.add( anEnvironmentList );
            }
        }

        final ComboBox seedsCountCombo = new ComboBox( "Choose number of seeds" );
        seedsCountCombo.setImmediate( true );
        seedsCountCombo.setNullSelectionAllowed( false );
        seedsCountCombo.setTextInputAllowed( false );
        seedsCountCombo.setValue( environmentWizard.getConfig() );

        seedsCountCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                environmentWizard.getConfig().setNumberOfSeeds( Integer.parseInt(
                        ( String ) event.getProperty().getValue() ) );
            }
        } );


        final ComboBox envCombo = new ComboBox( "Choose environment" );
        BeanItemContainer<Environment> eBean = new BeanItemContainer<>( Environment.class );
        eBean.addAll( envList );
        envCombo.setContainerDataSource( eBean );
        envCombo.setNullSelectionAllowed( false );
        envCombo.setTextInputAllowed( false );
        envCombo.setItemCaptionPropertyId( "name" );
        envCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                Environment e = ( Environment ) event.getProperty().getValue();
                environmentWizard.getConfig().setEnvironmentId( e.getId() );
                seedsCountCombo.removeAllItems();
                for ( int i=0; i < e.getContainerHosts().size(); i++ ){
                    seedsCountCombo.addItem( (i+1)+ "" );
                }
            }
        } );


        Button next = new Button( "Next" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( Strings.isNullOrEmpty( environmentWizard.getConfig().getClusterName() ) )
                {
                    show( "Please provide cluster name !" );
                }
                else if ( Strings.isNullOrEmpty( environmentWizard.getConfig().getDomainName() ) )
                {
                    show( "Please provide domain name !" );
                }
                else if ( envCombo.getValue() == null || seedsCountCombo.getValue() == null )
                {
                    show( "Please provide number of nodes and seeds !" );
                }
                else if ( ( ( Environment ) envCombo.getValue() ).getContainerHosts().size() <= Integer.parseInt(
                        ( String ) seedsCountCombo.getValue() ))
                {
                    show( "Number of seeds should be smaller than total number nodes in the cluster !" );
                }
                else
                {
                    environmentWizard.next();
                }
            }
        } );

        Button back = new Button( "Back" );
        back.addStyleName( "default" );
        back.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                environmentWizard.back();
            }
        } );

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing( true );
        layout.addComponent( new Label( "Please, specify installation settings" ) );
        layout.addComponent( content );

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent( back );
        buttons.addComponent( next );

        content.addComponent( clusterNameTxtFld );
        content.addComponent( domainNameTxtFld );
        content.addComponent( dataDirectoryTxtFld );
        content.addComponent( commitLogDirectoryTxtFld );
        content.addComponent( savedCachesDirectoryTxtFld );
        content.addComponent( envCombo );
        content.addComponent( seedsCountCombo );
        content.addComponent( buttons );

        addComponent( layout );
    }


    private boolean isTemplateExists( Set<ContainerHost> containerHosts, String templateName ){
        for ( ContainerHost host: containerHosts ){
            if ( host.getTemplateName().equals( templateName ) ){
                return true;
            }
        }
        return  false;
    }

    private int getNumberOfContainers( Environment environment, String productTemplateName ){
        int count = 0;
        for ( ContainerHost host : environment.getContainerHosts() ){
            if ( host.getTemplateName().equals( productTemplateName ) ){
                count++;
            }
        }
        return count;
    }

    private void show( String notification )
    {
        Notification.show( notification );
    }
}
