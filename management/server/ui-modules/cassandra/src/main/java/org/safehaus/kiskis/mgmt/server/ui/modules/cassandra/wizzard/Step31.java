/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

/**
 * @author bahadyr
 */
public class Step31 extends Panel {

    GridLayout grid;
    VerticalLayout verticalLayoutForm;
    Button next;

    public Step31(final CassandraWizard cassandraWizard) {
        setCaption("Configuration");
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(600, Sizeable.UNITS_PIXELS);
        verticalLayout.setMargin(true);

        grid = new GridLayout(6, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Install Wizard<br>"
                + " 1) Welcome<br>"
                + " 2) List nodes<br>"
                + " 3) <font color=\"#f14c1a\"><strong>Installation</strong></font><br>"
                + " 4) Configuration");
        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);

        grid.addComponent(menu, 0, 0, 1, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayout.setSpacing(true);

        Label label = new Label("Please enter the list of hosts to be included in the cluster");
        label.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(label);

        Label label1 = new Label("<strong>Status of nodes</strong><br>"
                + "<br>");
        label1.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(label1);

        grid.addComponent(verticalLayoutForm, 2, 0, 5, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.MIDDLE_CENTER);

        next = new Button("Next");
        next.setEnabled(false);
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cassandraWizard.showNext();
            }
        });
        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cassandraWizard.showBack();
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);
    }

    void updateUI(String msg) {
        Label label1 = new Label("<strong>" + msg + "</strong><br>");
        label1.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(label1);
        next.setEnabled(true);
    }

}
