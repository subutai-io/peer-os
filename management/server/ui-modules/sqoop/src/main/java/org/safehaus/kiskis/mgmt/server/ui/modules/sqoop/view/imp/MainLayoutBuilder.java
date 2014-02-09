package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.imp;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Layout;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UIUtil;

public class MainLayoutBuilder {

    static Layout create() {

        AbsoluteLayout layout = new AbsoluteLayout();

        layout.addComponent(UIUtil.getLabel("<h1>Sqoop Import</h1>", 200, 40), "left: 30px; top: 10px;");
        layout.addComponent(UIUtil.getLabel("Please select a data source type" , 200, 40), "left: 30px; top: 80px;");
        layout.addComponent(UIUtil.getButton("HDFS", 120, ImportLayoutBuilder.getListener(LayoutType.HDFS)), "left: 30px; top: 120px;");
        layout.addComponent(UIUtil.getButton("HBase", 120, ImportLayoutBuilder.getListener(LayoutType.HBASE)), "left: 30px; top: 160px;");
        layout.addComponent(UIUtil.getButton("Hive", 120, ImportLayoutBuilder.getListener(LayoutType.HIVE)), "left: 30px; top: 200px;");

        return layout;
    }
}
