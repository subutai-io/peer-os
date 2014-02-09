package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action;

import com.vaadin.ui.AbstractTextField;
import org.apache.commons.lang3.StringUtils;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;

public class ValidationAction implements Action {

    protected UILogger logger;
    protected AbstractTextField connectStringField;
    protected AbstractTextField usernameField;
    protected AbstractTextField passwordField;

    public ValidationAction(UILogger logger, AbstractTextField connectStringField, AbstractTextField usernameField, AbstractTextField passwordField) {
        this.logger = logger;
        this.connectStringField = connectStringField;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
    }

    @Override
    public void execute(Context context, Chain chain) {
        if (validate(context, chain)) {
            chain.proceed(context);
        }
    }

    protected boolean validate(Context context, Chain chain) {

        String connectString = (String) connectStringField.getValue();
        String username = (String) usernameField.getValue();
        String password = (String) passwordField.getValue();

        if (!checkValue(connectString, "Please enter the connect string")
                || !checkValue(username, "Please enter username")
                || !checkValue(password, "Please enter password")) {
            return false;
        }

        context.put("connectString", connectString);
        context.put("username", username);
        context.put("password", password);

        return subValidate(context, chain);
    }

    protected boolean subValidate(Context context, Chain chain) {
        return true;
    }

    protected boolean checkValue(String value, String errorMessage) {

        if (StringUtils.isEmpty(value)) {
            logger.info(errorMessage);
            return false;
        }

        return true;
    }
}
