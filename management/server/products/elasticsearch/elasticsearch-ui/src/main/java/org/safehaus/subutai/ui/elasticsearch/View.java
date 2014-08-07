package org.safehaus.subutai.ui.elasticsearch;


import java.util.Set;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.elasticsearch.ElasticSearch;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.ui.elasticsearch.component.AgentTree;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;


public class View extends CustomComponent {

    private final AgentManager agentManager;
    private ElasticSearch elasticSearch;

    private AgentTree agentTree;
    private final TextArea logTextArea = new TextArea( "Log:" );
    private final ComboBox configComboBox = new ComboBox( "Config:" );
    private final TextField configValueField = new TextField("Value:");


    public View( AgentManager agentManager, ElasticSearch elasticSearch ) {
        this.agentManager = agentManager;
        this.elasticSearch = elasticSearch;

        initContent();
    }


    private void initContent() {

        setHeight( "100%" );

        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        splitPanel.setSplitPosition( 20 );

        agentTree = new AgentTree( agentManager );
        splitPanel.setFirstComponent( agentTree );
        splitPanel.setSecondComponent( getMainLayout() );

        setCompositionRoot( splitPanel );
    }


    private Layout getMainLayout() {

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setWidth( 1200, Unit.PIXELS );
        layout.setHeight( 1000, Unit.PIXELS );

        layout.addComponent( logTextArea, "left: 200px; top: 50px;" );
        logTextArea.setRows( 30 );
        logTextArea.setWidth( "700px" );

        addMainButtons( layout );
        addConfigItems( layout );

        return layout;
    }


    private void addConfigItems( AbsoluteLayout layout ) {

        configComboBox.setNullSelectionAllowed( false );
        configComboBox.setTextInputAllowed( false );
        configComboBox.addItem( "cluster.name" );
        configComboBox.addItem( "node.name" );
        configComboBox.addItem( "node.name" );
        configComboBox.addItem( "node.master" );
        configComboBox.addItem( "node.data" );
        configComboBox.addItem( "index.number_of_shards" );
        configComboBox.addItem( "index.number_of_replicas" );

        layout.addComponent( configComboBox, "left: 20px; top: 400px;" );
        layout.addComponent( configValueField, "left: 20px; top: 450px;" );

        Button statusButton = addButton( layout, "Submit", 500 );
        statusButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                confSubmitButtonClicked();
            }
        } );
    }


    private void addMainButtons( AbsoluteLayout layout ) {

        Button statusButton = addButton( layout, "Status", 50 );
        statusButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                statusButtonClicked();
            }
        } );

        Button startButton = addButton( layout, "Start", 90 );
        startButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                startButtonClicked();
            }
        } );

        Button stopButton = addButton( layout, "Stop", 130 );
        stopButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                stopButtonClicked();
            }
        } );

        Button installButton = addButton( layout, "Install", 200 );
        installButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                installButtonClicked();
            }
        } );

        Button removeButton = addButton( layout, "Remove", 240 );
        removeButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                removeButtonClicked();
            }
        } );
    }


    private static Button addButton( AbsoluteLayout layout, String name, int top ) {

        Button button = new Button( name );
        button.setWidth( "150px" );
        layout.addComponent( button, String.format( "left: 20px; top: %spx;", top ) );

        return button;
    }


    private Agent getSelectedAgent() {

        Set<Agent> agents = agentTree.getSelectedAgents();

        return agents == null || agents.size() == 0
                ? null
                : agents.iterator().next();
    }


    private void addLog( String log ) {
        logTextArea.setValue( logTextArea.getValue() + "\n" + log );
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void confSubmitButtonClicked() {

        Agent agent = getSelectedAgent();

        if ( agent == null ) {
            addLog( "Please select a node!" );
            return;
        }

        String configKey = (String) configComboBox.getValue();
        String configValue = configValueField.getValue();

        if ( StringUtils.isEmpty( configKey ) || StringUtils.isEmpty( configKey ) ) {
            addLog( "Please enter config values!" );
            return;
        }

        AgentResult agentResult = elasticSearch.config( agent, configKey, configValue );
        String message = agentResult.getExitCode() == 0
                         ? "Command executed successfully"
                         : "Error to execute command. Please see logs for details.";

        addLog( message );
    }


    private void removeButtonClicked() {

        addLog( "Checking status..." );

        Agent agent = getSelectedAgent();

        if ( agent == null ) {
            addLog( "Please select a node!" );
            return;
        }

        AgentResult statusResult = elasticSearch.serviceStatus( agent );

        if ( statusResult.getExitCode() == 256 ) {
            addLog( "Elasticsearch NOT installed" );
            return;
        }

        addLog( "Removing Elasticsearch..." );

        AgentResult removeResult = elasticSearch.remove( agent );
        String message = removeResult.getExitCode() == 0
                         ? "Elasticsearch removed successfully"
                         : "Error to remove Elasticsearch. Please see logs for details.";

        addLog( message );
    }


    private void installButtonClicked() {

        addLog( "Checking status..." );

        Agent agent = getSelectedAgent();

        if ( agent == null ) {
            addLog( "Please select a node!" );
            return;
        }

        AgentResult agentResult = elasticSearch.serviceStatus( agent );

        if ( agentResult.getExitCode() != 256 ) {
            addLog( "Elasticsearch ALREADY installed" );
            return;
        }

        addLog( "Installing Elasticsearch..." );

        AgentResult installResult = elasticSearch.install( agent );
        String message = installResult.getExitCode() == 0
                         ? "Elasticsearch installed successfully"
                         : "Error to install Elasticsearch. Please see logs for details.";

        addLog( message );
    }


    private void statusButtonClicked() {

        addLog( "Checking status..." );

        Agent agent = getSelectedAgent();

        if ( agent == null ) {
            addLog( "Please select a node!" );
            return;
        }

        AgentResult agentResult = elasticSearch.serviceStatus( agent );

        switch ( agentResult.getExitCode() ) {
            case 0:
                addLog( "Elasticsearch installed and running" );
                break;
            case 768:
                addLog( "Elasticsearch installed and NOT running" );
                break;
            default:
                addLog( "Elasticsearch NOT installed" );
                break;
        }
    }


    private void startButtonClicked() {

        addLog( "Checking status..." );

        Agent agent = getSelectedAgent();

        if ( agent == null ) {
            addLog( "Please select a node!" );
            return;
        }

        AgentResult agentResult = elasticSearch.serviceStart( agent );

        switch ( agentResult.getExitCode() ) {
            case 0:
                addLog( "Elasticsearch started successfully" );
                break;
            default:
                addLog( "Error to start Elasticsearch" );
                break;
        }
    }


    private void stopButtonClicked() {

        addLog( "Checking status..." );

        Agent agent = getSelectedAgent();

        if ( agent == null ) {
            addLog( "Please select a node!" );
            return;
        }

        AgentResult agentResult = elasticSearch.serviceStop( agent );

        switch ( agentResult.getExitCode() ) {
            case 0:
                addLog( "Elasticsearch stopped successfully" );
                break;
            default:
                addLog( "Error to stop Elasticsearch" );
                break;
        }
    }
}
