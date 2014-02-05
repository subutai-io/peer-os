package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.export;

import com.vaadin.ui.AbstractTextField;
import org.apache.commons.lang3.StringUtils;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ValidationAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Set;

public class ExportValidationAction extends ValidationAction {

    private AbstractTextField tableField;
    private AbstractTextField hdfsPathField;

    public ExportValidationAction(UILogger logger, AbstractTextField connectStringField, AbstractTextField usernameField,AbstractTextField passwordField,
                                  AbstractTextField tableField, AbstractTextField hdfsPathField) {

        super(logger, connectStringField, usernameField, passwordField);
        this.tableField = tableField;
        this.hdfsPathField = hdfsPathField;
    }

    @Override
    protected boolean subValidate(Context context, Chain chain) {

        String table = (String) tableField.getValue();
        String hdfsPath = (String) hdfsPathField.getValue();

        if (!checkValue(table, "Please enter table name")
                || !checkValue(hdfsPath, "Please enter the HDFS path")) {
            return false;
        }

        context.put("table", table);
        context.put("hdfsPath", hdfsPath);

        return true;
    }

}
