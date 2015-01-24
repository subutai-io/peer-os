package org.safehaus.subutai.core.env.ui;


import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.ui.forms.BlueprintForm;
import org.safehaus.subutai.core.env.ui.forms.EnvironmentForm;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


public class EnvironmentManagerComponent extends CustomComponent
{

    public EnvironmentManagerComponent( final EnvironmentManager environmentManager, final PeerManager peerManager )
    {

        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        final TabSheet formSheet = new TabSheet();
        formSheet.setStyleName( Runo.TABSHEET_SMALL );
        formSheet.setSizeFull();

        BlueprintForm blueprintForm = new BlueprintForm( environmentManager, peerManager );
        formSheet.addTab( blueprintForm.getContentRoot(), "Blueprints" );
        formSheet.getTab( 0 ).setId( "Blueprints" );
        final EnvironmentForm environmentForm = new EnvironmentForm( environmentManager );
        formSheet.addTab( environmentForm.getContentRoot(), "Environments" );
        formSheet.getTab( 1 ).setId( "Environments" );

        verticalLayout.addComponent( formSheet );

        setCompositionRoot( verticalLayout );
    }
}
