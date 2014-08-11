package org.safehaus.subutai.ui.elasticsearch2.configuration;


import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;


public class ConfigurationView extends VerticalLayout {

    public ConfigurationView() {
        final ClustersTable clustersTable = new ClustersTable();
        Button viewClustersButton = new Button( "View clusters" );
        viewClustersButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                Object[] o = getClusters();
                clustersTable.refreshData(o);

            }
        } );
        addComponent( viewClustersButton );
        addComponent( clustersTable );
    }

    private Object[] getClusters() {
        Object[] o = {"Environment1", new Button("Configure"),
        "Environment2", new Button("Configure")};
        return o;
    }


}
