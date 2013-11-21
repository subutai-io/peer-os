/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.wizzard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

/**
 * @author bahadyr
 */
public class Step42 extends Panel {

    private static final String[] cities = new String[]{"cassandra-node1", "cassandra-node2", "cassandra-node3",
            "cassandra-node4", "cassandra-node5"};

    public Step42(final CassandraWizard aThis) {
        setCaption("Configuration");
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(600, Sizeable.UNITS_PIXELS);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(6, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Install Wizard<br>" +
                " 1) Welcome<br>" +
                " 2) List nodes<br>" +
                " 3) Installation<br>" +
                " 4) <font color=\"#f14c1a\"><strong>Configuration</strong></font><br>" +
                " 5) Start");
        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);

        grid.addComponent(menu, 0, 0, 1, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);


        Label label = new Label("<strong>Choose your seeds</strong>");
        label.setContentMode(Label.CONTENT_XHTML);

        grid.addComponent(label, 2, 0, 5, 0);
        grid.setComponentAlignment(label, Alignment.TOP_CENTER);

        Label label1 = new Label("Please drag and drop your nodes into seeds field" +
                "<br>");
        label1.setContentMode(Label.CONTENT_XHTML);

        grid.addComponent(label1, 2, 1, 5, 1);
        grid.setComponentAlignment(label1, Alignment.TOP_CENTER);


        TwinColSelect l = new TwinColSelect();
        for (String citie : cities) {
            l.addItem(citie);
        }
        l.setRows(10);
        l.setNullSelectionAllowed(true);
        l.setMultiSelect(true);
        l.setImmediate(true);
        l.setLeftColumnCaption("Seeds");
        l.setRightColumnCaption("Available Nodes");
        l.setWidth("350px");
        grid.addComponent(l, 2, 2, 5, 9);
        grid.setComponentAlignment(l, Alignment.TOP_LEFT);


        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                aThis.showNext();
            }
        });
        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                aThis.showBack();
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);
    }

}
