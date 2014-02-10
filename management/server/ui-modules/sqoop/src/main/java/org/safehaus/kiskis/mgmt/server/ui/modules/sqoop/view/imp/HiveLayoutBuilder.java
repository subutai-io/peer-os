package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.imp;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.imp.HiveValidationAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UIUtil;

public class HiveLayoutBuilder {

    public static Layout create() {

        TextArea textArea = UIUtil.getTextArea(800, 600);
        UILogger logger = new UILogger(textArea);

        AbstractTextField connectStringField = UIUtil.getTextField("Connection string:", 300);
        CheckBox allTablesCheckbox = new CheckBox("Import all tables");
        AbstractTextField tableField = UIUtil.getTextField("Table name:", 300);
        AbstractTextField usernameField = UIUtil.getTextField("Username:", 300);
        AbstractTextField passwordField = UIUtil.getTextField("Password:", 300, true);
        AbstractTextField hiveDatabaseField = UIUtil.getTextField("Database:", 300);
        AbstractTextField hiveTableField = UIUtil.getTextField("Table name:", 300);

        HiveValidationAction validationAction = new HiveValidationAction(logger, connectStringField, usernameField, passwordField, allTablesCheckbox, tableField,
                hiveDatabaseField, hiveTableField);
        ChainManager chainManager = new ChainManager(logger);
        Chain chain = chainManager.getChain(ChainManager.IMPORT_HIVE_COMMAND, "Import started, please wait...", validationAction);

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.addComponent(UIUtil.getLabel("<h1>Sqoop Import</h1>" , 200, 40), "left: 30px; top: 10px;");
        layout.addComponent(UIUtil.getLabel("<b>Hive</b>" , 200, 40), "left: 30px; top: 50px;");

        layout.addComponent(connectStringField, "left: 30px; top: 100px;");
        layout.addComponent(allTablesCheckbox, "left: 30px; top: 150px;");
        layout.addComponent(tableField, "left: 30px; top: 190px;");
        layout.addComponent(usernameField, "left: 30px; top: 250px;");
        layout.addComponent(passwordField, "left: 30px; top: 300px;");

        layout.addComponent(UIUtil.getLabel("<b>Hive parameters</b>" , 200, 40), "left: 30px; top: 360px;");
        layout.addComponent(hiveDatabaseField, "left: 30px; top: 400px;");
        layout.addComponent(hiveTableField, "left: 30px; top: 440px;");

        layout.addComponent(UIUtil.getButton("Back", 120, ImportLayoutBuilder.getListener(LayoutType.MAIN)), "left: 30px; top: 500px;");
        layout.addComponent(UIUtil.getButton("Import", 120, chain), "left: 160px; top: 500px;");
        layout.addComponent(textArea, "left: 380px; top: 100px;");

        return layout;
    }
}
