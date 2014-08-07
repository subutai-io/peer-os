package org.safehaus.subutai.ui.elasticsearch;


import java.util.Set;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.elasticsearch.ElasticSearch;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.ui.elasticsearch.component.AgentTree;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;


public class View extends CustomComponent {

    private final AgentManager agentManager;
    private ElasticSearch elasticSearch;

    private AgentTree agentTree;
    private TextArea logTextArea = new TextArea( "Log:" );

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

        addButtons( layout );

        return layout;
    }


    private void addButtons( AbsoluteLayout layout ) {

        Button statusButton = addButton( layout, "Status", 50 );
        statusButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                statusButtonClicked();
            }
        } );

        Button installButton = addButton( layout, "Install", 90 );
        installButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                installButtonClicked();
            }
        } );

        Button removeButton = addButton( layout, "Remove", 130 );
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
}
