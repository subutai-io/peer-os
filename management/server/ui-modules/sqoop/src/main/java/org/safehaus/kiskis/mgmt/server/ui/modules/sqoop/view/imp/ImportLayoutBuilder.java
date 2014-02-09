package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.imp;

import com.vaadin.ui.*;

public class ImportLayoutBuilder {

    private static AbsoluteLayout container = new AbsoluteLayout();
    private static Layout mainLayout = MainLayoutBuilder.create();
    private static Layout hdfsLayout = HdfsLayoutBuilder.create();
    private static Layout hbaseLayout = HBaseLayoutBuilder.create();
    private static Layout hiveLayout = HiveLayoutBuilder.create();

    public static Layout create() {
        showLayout(LayoutType.MAIN);
        return container;
    }

    static void showLayout(LayoutType layoutType) {

        container.removeAllComponents();
        Layout layout = mainLayout;

        switch (layoutType) {
        case HDFS:
            layout = hdfsLayout;
            break;
        case HBASE:
            layout = hbaseLayout;
            break;
        case HIVE:
            layout = hiveLayout;
            break;
        }

        container.addComponent(layout);
    }

    static Button.ClickListener getListener(final LayoutType layoutType) {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ImportLayoutBuilder.showLayout(layoutType);
            }
        };
    }
}
