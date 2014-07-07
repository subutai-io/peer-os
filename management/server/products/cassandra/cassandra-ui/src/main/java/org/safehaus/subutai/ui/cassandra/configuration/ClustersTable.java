package org.safehaus.subutai.ui.cassandra.configuration;


import com.vaadin.ui.Button;
import com.vaadin.ui.Table;


/**
 * Created by bahadyr on 7/4/14.
 */
public class ClustersTable extends Table {


    public ClustersTable() {
        setSizeFull();
        addContainerProperty( "Environment", String.class, null );
        addContainerProperty( "Configure", Button.class, null );
    }


    public void refreshData( Object[] o ) {
        addItem( o );
    }
}
