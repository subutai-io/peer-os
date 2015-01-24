package org.safehaus.subutai.core.env.ui;


import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.ui.forms.BlueprintForm;
import org.safehaus.subutai.core.env.ui.forms.EnvironmentForm;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


public class EnvironmentManagerComponent extends CustomComponent implements Disposable
{

    private EnvironmentManager environmentManager;
    private PeerManager peerManager;
    private TabSheet formSheet;
    private EnvironmentForm environmentForm;


    public EnvironmentManagerComponent( final EnvironmentManager environmentManager, final PeerManager peerManager )
    {

        this.environmentManager = environmentManager;
        this.peerManager = peerManager;

        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        formSheet = new TabSheet();
        formSheet.setStyleName( Runo.TABSHEET_SMALL );
        formSheet.setSizeFull();

        BlueprintForm blueprintForm = new BlueprintForm( this, environmentManager, peerManager );
        formSheet.addTab( blueprintForm.getContentRoot(), "Blueprints" );
        formSheet.getTab( 0 ).setId( "Blueprints" );
        environmentForm = new EnvironmentForm( environmentManager );
        formSheet.addTab( environmentForm.getContentRoot(), "Environments" );
        formSheet.getTab( 1 ).setId( "Environments" );

        verticalLayout.addComponent( formSheet );

        setCompositionRoot( verticalLayout );
    }


    public void focusEnvironmentForm()
    {
        formSheet.setSelectedTab( 1 );
        environmentForm.updateEnvironmentsTable();
    }


    @Override
    public void dispose()
    {
        //not ready yet
    }
}
