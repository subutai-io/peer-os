package org.safehaus.kiskis.mgmt.ui.hive.query;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

public class HiveQueryForm extends CustomComponent {

    public HiveQueryForm() {
        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();
        TabSheet mongoSheet = new TabSheet();
        mongoSheet.setStyleName(Runo.TABSHEET_SMALL);
        mongoSheet.setSizeFull();

        verticalLayout.addComponent(mongoSheet);
        setCompositionRoot(verticalLayout);
    }

}
