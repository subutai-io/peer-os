package org.safehaus.subutai.plugin.oozie.ui.wizard;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.data.Property;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import com.google.common.base.Strings;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;


public class StepSetConfig extends Panel
{

    public StepSetConfig( final Wizard wizard )
    {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight( 100, Unit.PERCENTAGE );
        verticalLayout.setMargin( true );

        GridLayout grid = new GridLayout( 10, 10 );
        grid.setSpacing( true );
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label( "Oozie Installation Wizard" );

        menu.setContentMode( ContentMode.HTML );
        panel.setContent( menu );
        grid.addComponent( menu, 0, 0, 2, 1 );
        //		grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setSpacing( true );

        Label configServersLabel = new Label( "<strong>Oozie Server</strong>" );
        configServersLabel.setContentMode( ContentMode.HTML );
        vl.addComponent( configServersLabel );

        final Label server = new Label( "Server" );
        vl.addComponent( server );

        final ComboBox cbServers = new ComboBox();
        cbServers.setImmediate( true );
        cbServers.setTextInputAllowed( false );
        cbServers.setRequired( true );
        cbServers.setNullSelectionAllowed( false );

        final HadoopClusterConfig hcc = wizard.getHadoopManager()
                .getCluster( wizard.getConfig().getHadoopClusterName() );
        for ( Agent agent : hcc.getAllNodes() )
        {
            cbServers.addItem( agent.getHostname() );
            cbServers.setItemCaption( agent.getHostname(), agent.getHostname() );
        }

        vl.addComponent( cbServers );

        if (  wizard.getConfig().getServer() != null )
        {
            cbServers.setValue( wizard.getConfig().getServer() );
        }

        final TwinColSelect selectClients = new TwinColSelect( "", new ArrayList<String>() );
        selectClients.setItemCaptionPropertyId( "hostname" );
        selectClients.setRows( 7 );
        selectClients.setNullSelectionAllowed( true );
        selectClients.setMultiSelect( true );
        selectClients.setImmediate( true );
        selectClients.setLeftColumnCaption( "Available nodes" );
        selectClients.setRightColumnCaption( "Client nodes" );
        selectClients.setWidth( 100, Unit.PERCENTAGE );
        selectClients.setContainerDataSource( new BeanItemContainer<>( Agent.class, hcc.getAllNodes() ) );

        if ( !CollectionUtil.isCollectionEmpty( wizard.getConfig().getClients() ) )
        {
            selectClients.setValue( wizard.getConfig().getClients() );
        }

        vl.addComponent( selectClients );

        grid.addComponent( vl, 3, 0, 9, 9 );
        grid.setComponentAlignment( vl, Alignment.TOP_CENTER );

        Button next = new Button( "Next" );
        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                wizard.getConfig().setServer( (String) cbServers.getValue() );
                Set<String> clientNodes = new HashSet<String>();
                if ( selectClients.getValue() != null ) {
                    for ( Agent node : (Set<Agent>) selectClients.getValue() ) {
                        clientNodes.add( node.getHostname() );
                    }
                    wizard.getConfig().setClients( clientNodes );
                }

                if ( wizard.getConfig().getServer() == null )
                {
                    show( "Please select node for Oozie server" );
                }
                else if ( wizard.getConfig().getClients() != null &&
                        wizard.getConfig().getClients().contains( wizard.getConfig().getServer() ) )
                {
                    show( "Oozie server and client can not be installed on the same host" );
                }
                else
                {
                    wizard.next();
                }
            }
        } );

        Button back = new Button( "Back" );
        back.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                wizard.back();
            }
        } );

        cbServers.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String selectedServerNode = event.getProperty().getValue().toString();
                List<Agent> hadoopNodes = hcc.getAllNodes();
                List<Agent> availableOozieClientNodes = new ArrayList<Agent>();
                availableOozieClientNodes.addAll(hadoopNodes);
                for ( Agent node : hadoopNodes ) {
                    if ( selectedServerNode.equals( node.getHostname() ) ) {
                        availableOozieClientNodes.remove( node );
                    }
                }
                selectClients.setContainerDataSource( new BeanItemContainer<>( Agent.class, availableOozieClientNodes ) );
                selectClients.markAsDirty();

            }
        });


        verticalLayout.addComponent( grid );

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent( back );
        horizontalLayout.addComponent( next );
        verticalLayout.addComponent( horizontalLayout );

        setContent( verticalLayout );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }
}
