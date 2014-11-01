package org.safehaus.subutai.core.metric.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.metric.api.Monitor;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;


public class MonitorForm extends CustomComponent
{

    private static final String MANAGER_TAB_CAPTION = "Metrics";


    public MonitorForm( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        final Monitor monitor = serviceLocator.getService( Monitor.class );
        final EnvironmentManager environmentManager = serviceLocator.getService( EnvironmentManager.class );


        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing( true );
        layout.setSizeFull();

        setCompositionRoot( layout );
    }
}
