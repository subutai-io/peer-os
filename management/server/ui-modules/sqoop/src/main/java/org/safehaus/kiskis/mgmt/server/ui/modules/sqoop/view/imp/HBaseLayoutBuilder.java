package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.imp;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.export.ExportValidationAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UIUtil;

public class HBaseLayoutBuilder {

    public static Layout create() {

        TextArea textArea = UIUtil.getTextArea(800, 600);
        UILogger logger = new UILogger(textArea);

        AbstractTextField connectStringField = UIUtil.getTextField("Connection string:", 300);
        AbstractTextField tableField = UIUtil.getTextField("Table name:", 300);
        AbstractTextField usernameField = UIUtil.getTextField("Username:", 300);
        AbstractTextField passwordField = UIUtil.getTextField("Password:", 300, true);
        AbstractTextField hdfsPathField = UIUtil.getTextField("HDFS file path:", 300);

        ExportValidationAction inputValidationAction = new ExportValidationAction(logger, connectStringField, tableField, usernameField, passwordField, hdfsPathField);
        ChainManager chainManager = new ChainManager(logger);

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.addComponent(UIUtil.getLabel("<h1>Sqoop Import</h1>" , 200, 40), "left: 30px; top: 10px;");
        layout.addComponent(UIUtil.getLabel("<b>HBase</b>" , 200, 40), "left: 30px; top: 50px;");



        layout.addComponent(UIUtil.getButton("Back", 120, ImportLayoutBuilder.getListener(LayoutType.MAIN)), "left: 30px; top: 380px;");
        layout.addComponent(UIUtil.getButton("Export", 120, chainManager.getExportChain(inputValidationAction)), "left: 160px; top: 380px;");
        layout.addComponent(textArea, "left: 380px; top: 100px;");

        return layout;
    }

}
