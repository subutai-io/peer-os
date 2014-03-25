package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.imp;

import com.vaadin.ui.*;

public class ImportLayout extends AbsoluteLayout {

    private final Layout MAIN_LAYOUT = MainLayoutBuilder.create(this);
    private final Layout HDFS_LAYOUT = HdfsLayoutBuilder.create(this);
    private final Layout HBASE_LAYOUT = HBaseLayoutBuilder.create(this);
    private final Layout HIVE_LAYOUT = HiveLayoutBuilder.create(this);

    public ImportLayout() {
        showLayout(LayoutType.MAIN);
    }

    private void showLayout(LayoutType layoutType) {
        removeAllComponents();
        addComponent( getLayout(layoutType) );
    }

    private Layout getLayout(LayoutType layoutType) {

        Layout layout;

        switch (layoutType) {
        case HDFS:
            layout = HDFS_LAYOUT;
            break;
        case HBASE:
            layout = HBASE_LAYOUT;
            break;
        case HIVE:
            layout = HIVE_LAYOUT;
            break;
        default:
            layout = MAIN_LAYOUT;
            break;
        }

        return layout;
    }

    Button.ClickListener getListener(final LayoutType layoutType) {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                showLayout(layoutType);
            }
        };
    }
}
