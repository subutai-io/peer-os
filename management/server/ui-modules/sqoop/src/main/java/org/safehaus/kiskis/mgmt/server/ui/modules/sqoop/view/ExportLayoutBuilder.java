package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.export.ExportChainManager;

public class ExportLayoutBuilder {

    public static Layout create() {

        TextArea textArea = UIUtil.getTextArea(600, 600);
        UILogger logger = new UILogger(textArea);
        ExportChainManager chainManager = new ExportChainManager(logger);
        AbsoluteLayout layout = new AbsoluteLayout();

        layout.addComponent(UIUtil.getButton("Export", 120, chainManager.getStatusChain()), "left: 20px; top: 30px;");
        layout.addComponent(textArea, "left: 180px; top: 30px;");

        return layout;
    }

}
