package org.safehaus.subutai.ui.hive.query;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.subutai.ui.hive.query.components.HiveQueryLayout;

public class HiveQueryForm extends CustomComponent {

    public HiveQueryForm() {
        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();
        TabSheet tabSheet = new TabSheet();
        tabSheet.setStyleName(Runo.TABSHEET_SMALL);
        tabSheet.setSizeFull();

        tabSheet.addComponent(new HiveQueryLayout());

        verticalLayout.addComponent(tabSheet);
        setCompositionRoot(verticalLayout);
    }

}
