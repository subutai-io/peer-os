package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.imp;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.imp.HdfsValidationAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UIUtil;

public class HdfsLayoutBuilder {

    public static Layout create(ImportLayout importLayout) {

        TextArea textArea = UIUtil.getTextArea(800, 600);
        UILogger logger = new UILogger(textArea);

        AbstractTextField connectStringField = UIUtil.getTextField("Connection string:", 300);
        CheckBox allTablesCheckbox = new CheckBox("Import all tables");
        AbstractTextField tableField = UIUtil.getTextField("Table name:", 300);
        AbstractTextField usernameField = UIUtil.getTextField("Username:", 300);
        AbstractTextField passwordField = UIUtil.getTextField("Password:", 300, true);

        HdfsValidationAction validationAction = new HdfsValidationAction(logger, connectStringField, usernameField, passwordField, allTablesCheckbox, tableField);
        ChainManager chainManager = new ChainManager(logger);
        Chain chain = chainManager.getChain(ChainManager.IMPORT_HDFS_COMMAND, "Import started, please wait...", validationAction);

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.addComponent(UIUtil.getLabel("<h1>Sqoop Import</h1>" , 200, 40), "left: 30px; top: 10px;");
        layout.addComponent(UIUtil.getLabel("<b>HDFS</b>" , 200, 40), "left: 30px; top: 50px;");

        layout.addComponent(connectStringField, "left: 30px; top: 100px;");
        layout.addComponent(allTablesCheckbox, "left: 30px; top: 150px;");
        layout.addComponent(tableField, "left: 30px; top: 190px;");
        layout.addComponent(usernameField, "left: 30px; top: 250px;");
        layout.addComponent(passwordField, "left: 30px; top: 300px;");

        layout.addComponent(UIUtil.getButton("Back", 120, importLayout.getListener(LayoutType.MAIN)), "left: 30px; top: 380px;");
        layout.addComponent(UIUtil.getButton("Import", 120, chain), "left: 160px; top: 380px;");
        layout.addComponent(textArea, "left: 380px; top: 100px;");

        return layout;
    }

}
