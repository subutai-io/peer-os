package org.safehaus.subutai.plugin.pig.ui.wizard;


import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.pig.api.Config;
import org.safehaus.subutai.plugin.pig.api.SetupType;
import org.safehaus.subutai.plugin.pig.ui.PigUI;

import java.util.*;


public class ConfigurationStep extends Panel
{

    public ConfigurationStep( final Wizard wizard )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 3 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );


        SetupType st = wizard.getConfig().getSetupType();

        if ( st == SetupType.OVER_HADOOP ) {
            addOverHadoopControls( content, wizard.getConfig() );
        }
        else if ( st == SetupType.WITH_HADOOP ) {
            addWithHadoopControls( content, wizard.getConfig(), wizard.getHadoopConfig() );
        }

        // -------------------------------------------------------------------------------------------------------------
        // Buttons

        Button next = new Button( "Next" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( Strings.isNullOrEmpty( wizard.getConfig().getHadoopClusterName() ) )
                {
                    show( "Please, enter Hadoop cluster" );
                }
                else
                {
                    wizard.next();
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
                wizard.back();
            }
        } );

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing( true );
        layout.addComponent( new Label( "Please, specify installation settings" ) );
        layout.addComponent( content );

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent( back );
        buttons.addComponent( next );

        content.addComponent( buttons );

        setContent( layout );

    }


    private void addOverHadoopControls(ComponentContainer parent, final Config config) {

        final TwinColSelect select = new TwinColSelect("Nodes", new ArrayList<Agent>());

        ComboBox hadoopClusters = new ComboBox("Hadoop cluster");
        hadoopClusters.setImmediate(true);
        hadoopClusters.setTextInputAllowed(false);
        hadoopClusters.setRequired(true);
        hadoopClusters.setNullSelectionAllowed(false);
        hadoopClusters.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if(event.getProperty().getValue() != null) {
                    HadoopClusterConfig hadoopInfo = (HadoopClusterConfig)event.getProperty().getValue();
                    select.setValue(null);
                    select.setContainerDataSource(
                            new BeanItemContainer<>(Agent.class, hadoopInfo.getAllNodes())
                    );
                    config.setHadoopClusterName( hadoopInfo.getClusterName() );
                    config.getNodes().clear();
                }
            }
        });

        Hadoop hadoopManager = PigUI.getHadoopManager();
        List<HadoopClusterConfig> clusters = hadoopManager.getClusters();
        if(clusters != null)
            for(HadoopClusterConfig hadoopClusterInfo : clusters) {
                hadoopClusters.addItem(hadoopClusterInfo);
                hadoopClusters.setItemCaption(hadoopClusterInfo,
                        hadoopClusterInfo.getClusterName());
            }

        String hcn = config.getHadoopClusterName();
        if(hcn != null) {
            HadoopClusterConfig info = hadoopManager.getCluster(hcn);
            if(info != null) hadoopClusters.setValue(info);
        } else if(clusters != null && clusters.size() > 0)
            hadoopClusters.setValue(clusters.iterator().next());

        select.setItemCaptionPropertyId("hostname");
        select.setRows(7);
        select.setMultiSelect(true);
        select.setImmediate(true);
        select.setLeftColumnCaption("Available Nodes");
        select.setRightColumnCaption("Selected Nodes");
        select.setWidth(100, Unit.PERCENTAGE);
        select.setRequired(true);
        if(config.getNodes() != null && !config.getNodes().isEmpty())
            select.setValue(config.getNodes());
        select.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if(event.getProperty().getValue() != null) {
                    Collection agentList = (Collection)event.getProperty().getValue();
                    config.getNodes().clear();
                    config.getNodes().addAll(agentList);
                }
            }
        });

        parent.addComponent(hadoopClusters);
        parent.addComponent(select);
    }


    private void addWithHadoopControls(ComponentContainer content, final Config config,
                                       final HadoopClusterConfig hadoopConfig) {

        Collection<Integer> col = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        final TextField txtHadoopClusterName = new TextField("Hadoop cluster name");
        txtHadoopClusterName.setRequired(true);
        txtHadoopClusterName.setMaxLength(20);
        if(hadoopConfig.getClusterName() != null)
            txtHadoopClusterName.setValue(hadoopConfig.getClusterName());
        txtHadoopClusterName.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String name = event.getProperty().getValue().toString().trim();
                config.setHadoopClusterName(name);
                hadoopConfig.setClusterName(name);
            }
        });

        ComboBox cmbSlaveNodes = new ComboBox("Number of Hadoop slave nodes", col);
        cmbSlaveNodes.setImmediate(true);
        cmbSlaveNodes.setTextInputAllowed(false);
        cmbSlaveNodes.setNullSelectionAllowed(false);
        cmbSlaveNodes.setValue(hadoopConfig.getCountOfSlaveNodes());
        cmbSlaveNodes.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                hadoopConfig.setCountOfSlaveNodes((Integer)event.getProperty().getValue());
            }
        });

        ComboBox cmbReplFactor = new ComboBox("Replication factor for Hadoop slave nodes", col);
        cmbReplFactor.setImmediate(true);
        cmbReplFactor.setTextInputAllowed(false);
        cmbReplFactor.setNullSelectionAllowed(false);
        cmbReplFactor.setValue(hadoopConfig.getReplicationFactor());
        cmbReplFactor.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                hadoopConfig.setReplicationFactor((Integer)event.getProperty().getValue());
            }
        });

        TextField txtHadoopDomain = new TextField("Hadoop cluster domain name");
        txtHadoopDomain.setInputPrompt(hadoopConfig.getDomainName());
        txtHadoopDomain.setValue(hadoopConfig.getDomainName());
        txtHadoopDomain.setMaxLength(20);
        txtHadoopDomain.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String val = event.getProperty().getValue().toString().trim();
                if(!val.isEmpty()) hadoopConfig.setDomainName(val);
            }
        });

        content.addComponent(new Label("Hadoop settings"));
        content.addComponent(txtHadoopClusterName);
        content.addComponent(cmbSlaveNodes);
        content.addComponent(cmbReplFactor);
        content.addComponent(txtHadoopDomain);
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }

}
