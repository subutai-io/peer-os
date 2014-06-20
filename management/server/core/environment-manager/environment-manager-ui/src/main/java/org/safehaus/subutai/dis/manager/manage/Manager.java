package org.safehaus.subutai.dis.manager.manage;


import org.safehaus.subutai.api.agentmanager.AgentManager;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
public class Manager extends VerticalLayout {

    private final static String physicalHostLabel = "DIS";
    private volatile boolean isDestroyAllButtonClicked = false;


    public Manager( AgentManager agentManager ) {

        setSpacing( true );
        setMargin( true );


        addComponent( new Label( "Hello" ) );
    }
}
