package org.safehaus.subutai.core.identity.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.server.ErrorMessage;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class ErrorUtils {
    public static List<String> getComponentError(
            final AbstractComponent[] componentArray) {
        List<String> errorList = new ArrayList<String>();

        for (AbstractComponent component : componentArray) {
            ErrorMessage errorMessage = component.getErrorMessage();
            if (errorMessage != null) {
                errorList.add(errorMessage.getFormattedHtmlMessage());
            }
        }

        return errorList;
    }

    public static List<String> getComponentError(
            final Collection<?> componentCollection) {
        AbstractComponent[] componentArray = componentCollection
                .toArray(new AbstractComponent[] {});
        return ErrorUtils.getComponentError(componentArray);
    }

    public static void showComponentErrors(
            final AbstractComponent[] componentArray) {
        List<String> errorList = ErrorUtils.getComponentError(componentArray);

        String error = StringUtils.join(errorList, "\n");

        Notification notification = new Notification("Error", error,
                Type.ERROR_MESSAGE, true);

        notification.show(Page.getCurrent());
    }

    public static void showComponentErrors(
            final Collection<?> componentCollection) {
        AbstractComponent[] componentArray = componentCollection
                .toArray(new AbstractComponent[] {});

        ErrorUtils.showComponentErrors(componentArray);
    }
}

