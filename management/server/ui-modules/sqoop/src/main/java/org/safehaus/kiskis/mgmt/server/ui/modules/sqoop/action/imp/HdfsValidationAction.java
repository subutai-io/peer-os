package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.imp;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.CheckBox;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ValidationAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;

public class HdfsValidationAction extends ValidationAction {

    private AbstractTextField tableField;
    private CheckBox allTablesCheckbox;

    public HdfsValidationAction(UILogger logger, AbstractTextField connectStringField, AbstractTextField usernameField, AbstractTextField passwordField,
                                CheckBox allTablesCheckbox, AbstractTextField tableField) {
        super(logger, connectStringField, usernameField, passwordField);
        this.allTablesCheckbox = allTablesCheckbox;
        this.tableField = tableField;
    }

    @Override
    protected boolean subValidate(Context context, Chain chain) {

        boolean allTables = (Boolean) allTablesCheckbox.getValue();
        String table = (String) tableField.getValue();

        if (!allTables && !checkValue(table, "Please enter table name")) {
            return false;
        }

        if (allTables) {
            context.put("importUtil", "sqoop-import-all-tables");
            context.put("tableOption", "");
        } else {
            context.put("importUtil", "sqoop-import");
            context.put("tableOption", "--table "+table);
        }

        return true;
    }

}
