package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view;

import com.vaadin.ui.Button;

public class UIStateManager {

    private static Button buttons[];

    public static void init(Button ... buttons) {
        UIStateManager.buttons = buttons;
    }

    public static void start() {
        enableButtons(false);
    }

    public static void end() {
        enableButtons(true);
        UILogger.info("Completed");
    }

    private static void enableButtons(boolean enabled) {
        for (Button button : buttons) {
            button.setEnabled(enabled);
        }
    }

}
