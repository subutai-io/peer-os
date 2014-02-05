package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.imp;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.CheckBox;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ValidationAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UIUtil;

public class HiveValidationAction extends ValidationAction {

    private AbstractTextField tableField;
    private CheckBox allTablesCheckbox;
    AbstractTextField hiveDatabaseField;
    AbstractTextField hiveTableField;

    public HiveValidationAction(UILogger logger, AbstractTextField connectStringField, AbstractTextField usernameField, AbstractTextField passwordField,
                                CheckBox allTablesCheckbox, AbstractTextField tableField, AbstractTextField hiveDatabaseField, AbstractTextField hiveTableField) {
        super(logger, connectStringField, usernameField, passwordField);
        this.allTablesCheckbox = allTablesCheckbox;
        this.tableField = tableField;
        this.hiveDatabaseField = hiveDatabaseField;
        this.hiveTableField = hiveTableField;
    }

    @Override
    protected boolean subValidate(Context context, Chain chain) {

        boolean allTables = (Boolean) allTablesCheckbox.getValue();
        String table = (String) tableField.getValue();
        String hiveDatabase = (String) hiveDatabaseField.getValue();
        String hiveTable = (String) hiveTableField.getValue();

        if (!checkValue(hiveDatabase, "Please enter Hive database")) {
            return false;
        } else if (!allTables && (!checkValue(table, "Please enter table name") || !checkValue(hiveTable, "Please enter Hive table name") )) {
            return false;
        }

        if (allTables) {
            context.put("importUtil", "sqoop-import-all-tables");
            context.put("tableOption", "");
        } else {
            context.put("importUtil", "sqoop-import");
            context.put("tableOption", String.format("--table %s --hive-table %s.%s", table, hiveDatabase, hiveTable));
        }

        return true;
    }

}
