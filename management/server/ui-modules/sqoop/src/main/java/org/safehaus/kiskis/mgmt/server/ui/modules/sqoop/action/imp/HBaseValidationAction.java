package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.imp;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.CheckBox;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ValidationAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;

public class HBaseValidationAction extends ValidationAction {

    private AbstractTextField tableField;
    AbstractTextField hbaseTableField;
    AbstractTextField hbaseColumnField;

    public HBaseValidationAction(UILogger logger, AbstractTextField connectStringField, AbstractTextField usernameField, AbstractTextField passwordField,
                                 AbstractTextField tableField, AbstractTextField hbaseTableField, AbstractTextField hbaseColumnField) {
        super(logger, connectStringField, usernameField, passwordField);
        this.tableField = tableField;
        this.hbaseTableField = hbaseTableField;
        this.hbaseColumnField = hbaseColumnField;
    }

    @Override
    protected boolean subValidate(Context context, Chain chain) {

        String table = (String) tableField.getValue();
        String hbaseTable = (String) hbaseTableField.getValue();
        String hbaseColumn = (String) hbaseColumnField.getValue();

        if (!checkValue(table, "Please enter table name")
                || !checkValue(hbaseTable, "Please enter HBase table name")
                || !checkValue(hbaseColumn, "Please enter HBase column")) {
            return false;
        }

        context.put("table", table);
        context.put("hbaseTable", hbaseTable);
        context.put("hbaseColumn", hbaseColumn);

        return true;
    }

}
