/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 * @author bahadyr
 */
public class Step1 extends Panel {

    public Step1(final CassandraWizard cassandraWizard) {
        setCaption("Welcome");
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(400, Sizeable.UNITS_PIXELS);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(6, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Install Wizard<br>"
                + " 1) <font color=\"#f14c1a\"><strong>Welcome</strong></font><br>"
                + " 2) List nodes<br>"
                + " 3) Installation<br>"
                + " 4) Configuration<br>");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 1, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        Label label = new Label("Welcome CassandraWizard installation wizard. "
                + "Please follow the steps carefully. "
                + "When you are finished the wizard, you will have complete "
                + "working cassandra servers. ");
        label.setContentMode(Label.CONTENT_XHTML);
        grid.addComponent(label, 2, 0, 5, 9);
        grid.setComponentAlignment(label, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cassandraWizard.showNext();
            }
        });

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(next);

        addComponent(verticalLayout);

        Task task = new Task();
        task.setDescription("CassandraModule Wizard installing");
        task.setTaskStatus(TaskStatus.NEW);
        String uuid = cassandraWizard.getCommandManager().saveTask(task);
        cassandraWizard.setTask(task);
    }

}
