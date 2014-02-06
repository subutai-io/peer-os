package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.export.ExportValidationAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;

public class ExportLayoutBuilder {

    public static Layout create() {

        TextArea textArea = UIUtil.getTextArea(800, 600);
        UILogger logger = new UILogger(textArea);

        AbstractTextField connectStringField = UIUtil.getTextField("Connection string:", 300);
        AbstractTextField tableField = UIUtil.getTextField("Table name:", 300);
        AbstractTextField usernameField = UIUtil.getTextField("Username:", 300);
        AbstractTextField passwordField = UIUtil.getTextField("Password:", 300, true);
        AbstractTextField hdfsPathField = UIUtil.getTextField("HDFS file path:", 300);

        ExportValidationAction validationAction = new ExportValidationAction(logger, connectStringField, usernameField, passwordField, tableField, hdfsPathField);
        ChainManager chainManager = new ChainManager(logger);
        Chain chain = chainManager.getChain(ChainManager.EXPORT_COMMAND, "Export started, please wait...", validationAction);

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.addComponent(UIUtil.getLabel("<h1>Sqoop Export</h1>" , 200, 40), "left: 30px; top: 10px;");
        layout.addComponent(connectStringField, "left: 30px; top: 100px;");
        layout.addComponent(tableField, "left: 30px; top: 150px;");
        layout.addComponent(usernameField, "left: 30px; top: 200px;");
        layout.addComponent(passwordField, "left: 30px; top: 250px;");
        layout.addComponent(hdfsPathField, "left: 30px; top: 320px;");
        layout.addComponent(UIUtil.getButton("Export", 120, chain), "left: 100px; top: 380px;");
        layout.addComponent(textArea, "left: 380px; top: 100px;");

        return layout;
    }

}
