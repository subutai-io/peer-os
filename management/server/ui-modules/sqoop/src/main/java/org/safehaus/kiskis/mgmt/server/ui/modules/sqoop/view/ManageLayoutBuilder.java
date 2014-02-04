package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ChainManager;

class ManageLayoutBuilder {

    static Layout create() {

        TextArea textArea = UIUtil.getTextArea(600, 600);
        UILogger logger = new UILogger(textArea);
        ChainManager chainManager = new ChainManager(logger);
        AbsoluteLayout layout = new AbsoluteLayout();

        layout.addComponent(UIUtil.getButton("Check Status", 120, chainManager.getStatusChain()), "left: 20px; top: 30px;");
        layout.addComponent(UIUtil.getButton("Install", 120, chainManager.getInstallChain()), "left: 20px; top: 70px;");
        layout.addComponent(UIUtil.getButton("Remove", 120, chainManager.getRemoveChain()), "left: 20px; top: 110px;");
        layout.addComponent(textArea, "left: 180px; top: 30px;");

        return layout;
    }
}
