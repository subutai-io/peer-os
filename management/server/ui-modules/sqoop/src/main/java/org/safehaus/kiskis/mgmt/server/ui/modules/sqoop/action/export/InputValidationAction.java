package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.export;

import com.vaadin.ui.AbstractTextField;
import org.apache.commons.lang3.StringUtils;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Set;

public class InputValidationAction implements Action {

    private UILogger logger;
    private AbstractTextField connectStringField;
    private AbstractTextField tableField;
    private AbstractTextField usernameField;
    private AbstractTextField passwordField;
    private AbstractTextField hdfsPathField;

    public InputValidationAction(UILogger logger, AbstractTextField connectStringField, AbstractTextField tableField, AbstractTextField usernameField,
                                 AbstractTextField passwordField, AbstractTextField hdfsPathField) {
        this.logger = logger;
        this.connectStringField = connectStringField;
        this.tableField = tableField;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.hdfsPathField = hdfsPathField;
    }

    @Override
    public void execute(Context context, Chain chain) {

        String connectString = (String) connectStringField.getValue();
        String table = (String) tableField.getValue();
        String username = (String) usernameField.getValue();
        String password = (String) passwordField.getValue();
        String hdfsPath = (String) hdfsPathField.getValue();

        if (!checkValue(connectString, "Please enter the connect string")
                || !checkValue(table, "Please enter table name")
                || !checkValue(username, "Please enter username")
                || !checkValue(password, "Please enter password")
                || !checkValue(hdfsPath, "Please enter HDFS file path")) {
            return;
        }

        context.put("connectString", connectString);
        context.put("table", table);
        context.put("username", username);
        context.put("password", password);
        context.put("hdfsPath", hdfsPath);

        chain.proceed(context);
    }

    private boolean checkValue(String value, String errorMessage) {

        if (StringUtils.isEmpty(value)) {
            logger.info(errorMessage);
            return false;
        }

        return true;
    }
}
