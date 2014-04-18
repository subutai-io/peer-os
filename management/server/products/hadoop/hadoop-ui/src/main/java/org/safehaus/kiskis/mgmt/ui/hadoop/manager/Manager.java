package org.safehaus.kiskis.mgmt.ui.hadoop.manager;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

/**
 * Created by daralbaev on 12.04.14.
 */
public class Manager extends Panel {
    private HorizontalLayout horizontalLayout;
    private Label indicator;
    private HadoopTable table;

    public Manager() {
        setSizeFull();

        horizontalLayout = new HorizontalLayout();
        horizontalLayout.setMargin(true);
        horizontalLayout.setSpacing(true);

        horizontalLayout.addComponent(getButtonRefresh());
        horizontalLayout.addComponent(getIndicator());

        addComponent(horizontalLayout);
        addComponent(getHadoopTable());
    }

    private Button getButtonRefresh() {
        Button button = new Button("Refresh");
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                table.refreshDataSource();
            }
        });

        return button;
    }

    private Label getIndicator() {
        indicator = new Label();
        indicator.setIcon(new ThemeResource("icons/indicator.gif"));
        indicator.setContentMode(Label.CONTENT_XHTML);
        indicator.setHeight(11, Sizeable.UNITS_PIXELS);
        indicator.setWidth(50, Sizeable.UNITS_PIXELS);
        indicator.setVisible(true);

        return indicator;
    }

    private HadoopTable getHadoopTable() {
        if (table == null) {
            table = new HadoopTable("Hadoop Clusters", indicator);
        }

        return table;
    }
}
