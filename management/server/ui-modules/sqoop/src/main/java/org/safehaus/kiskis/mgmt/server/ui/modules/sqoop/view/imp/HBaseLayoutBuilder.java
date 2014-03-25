package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.imp;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.imp.HBaseValidationAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UIUtil;

public class HBaseLayoutBuilder {

    public static Layout create(ImportLayout importLayout) {

        TextArea textArea = UIUtil.getTextArea(800, 600);
        UILogger logger = new UILogger(textArea);

        AbstractTextField connectStringField = UIUtil.getTextField("Connection string:", 300);
        AbstractTextField tableField = UIUtil.getTextField("Table name:", 300);
        AbstractTextField usernameField = UIUtil.getTextField("Username:", 300);
        AbstractTextField passwordField = UIUtil.getTextField("Password:", 300, true);
        AbstractTextField hbaseTableField = UIUtil.getTextField("Table name:", 300);
        AbstractTextField hbaseColumnField = UIUtil.getTextField("Column family:", 300);

        HBaseValidationAction validationAction = new HBaseValidationAction(logger, connectStringField, usernameField, passwordField, tableField, hbaseTableField, hbaseColumnField);
        ChainManager chainManager = new ChainManager(logger);
        Chain chain = chainManager.getChain(ChainManager.IMPORT_HBASE_COMMAND, "Import started, please wait...", validationAction);

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.addComponent(UIUtil.getLabel("<h1>Sqoop Import</h1>" , 200, 40), "left: 30px; top: 10px;");
        layout.addComponent(UIUtil.getLabel("<b>HBase</b>" , 200, 40), "left: 30px; top: 50px;");

        layout.addComponent(connectStringField, "left: 30px; top: 100px;");
        layout.addComponent(tableField, "left: 30px; top: 150px;");
        layout.addComponent(usernameField, "left: 30px; top: 200px;");
        layout.addComponent(passwordField, "left: 30px; top: 250px;");

        layout.addComponent(UIUtil.getLabel("<b>HBase parameters</b>" , 200, 40), "left: 30px; top: 310px;");
        layout.addComponent(hbaseTableField, "left: 30px; top: 350px;");
        layout.addComponent(hbaseColumnField, "left: 30px; top: 400px;");

        layout.addComponent(UIUtil.getButton("Back", 120, importLayout.getListener(LayoutType.MAIN)), "left: 30px; top: 500px;");
        layout.addComponent(UIUtil.getButton("Import", 120, chain), "left: 160px; top: 500px;");
        layout.addComponent(textArea, "left: 380px; top: 100px;");

        return layout;
    }
}
