package org.safehaus.subutai.ui.elasticsearch;


import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.ui.elasticsearch.component.AgentTree;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;


public class View extends CustomComponent {

    private final AgentManager agentManager;

    private AgentTree agentTree;

    public View( AgentManager agentManager ) {
        this.agentManager = agentManager;

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
                System.out.println( ">> install" );
            }
        } );
    }


    private static Button addButton( AbsoluteLayout layout, String name, int top ) {
        Button button = new Button( name );
        button.setWidth( "150px" );
        layout.addComponent( button, String.format( "left: 20px; top: %spx;", top ) );

        return button;
    }


    private void statusButtonClicked() {
        System.out.println( ">> selected agent: " + getSelectedNode() );
    }


    private String getSelectedNode() {

        Set<Agent> agents = agentTree.getSelectedAgents();

        return agents == null || agents.size() == 0
                ? null
                : agents.iterator().next().getHostname();
    }


}
