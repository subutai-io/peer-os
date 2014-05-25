package org.safehaus.subutai.ui.hadoop;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.subutai.ui.hadoop.manager.Manager;
import org.safehaus.subutai.ui.hadoop.wizard.Wizard;

/**
 * Created by daralbaev on 08.04.14.
 */
public class HadoopForm extends CustomComponent {
    private final Wizard wizard;
    private final Manager manager;

    public HadoopForm() {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName(Runo.TABSHEET_SMALL);
        sheet.setSizeFull();

        manager = new Manager();
        wizard = new Wizard();
        sheet.addTab(wizard.getContent(), "Install");
        sheet.addTab(manager, "Manage");


        verticalLayout.addComponent(sheet);
        setCompositionRoot(verticalLayout);
    }
}
