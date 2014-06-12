/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.dis.manager;


import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.server.ui.MgmtAgentManager;
import org.safehaus.subutai.server.ui.MgmtApplication;
import org.safehaus.subutai.shared.protocol.Disposable;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 *
 */
public class DisForm extends CustomComponent implements Disposable {

    private final static String managerTabCaption = "Manage";
    private final MgmtAgentManager agentTree;


    public DisForm( AgentManager agentManager ) {
        setHeight( 100, UNITS_PERCENTAGE );

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName( Runo.SPLITPANEL_SMALL );
        horizontalSplit.setSplitPosition( 200, UNITS_PIXELS );
        agentTree = MgmtApplication.createAgentTree();
        horizontalSplit.setFirstComponent( agentTree );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();
        TabSheet commandsSheet = new TabSheet();
        commandsSheet.setStyleName( Runo.TABSHEET_SMALL );
        commandsSheet.setSizeFull();

        verticalLayout.addComponent( commandsSheet );

        horizontalSplit.setSecondComponent( verticalLayout );
        setCompositionRoot( horizontalSplit );
    }


    public void dispose() {
        agentTree.dispose();
    }
}
