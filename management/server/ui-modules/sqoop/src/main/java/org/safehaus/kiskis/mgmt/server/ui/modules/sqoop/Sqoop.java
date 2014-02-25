package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.ModuleComponent;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class Sqoop implements Module {

    private static final String MODULE_NAME = "Sqoop";
    private static TaskRunner taskRunner;

    public void setTaskRunner(TaskRunner taskRunner) {
        Sqoop.taskRunner = taskRunner;
    }

    public static TaskRunner getTaskRunner() {
        return taskRunner;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(MODULE_NAME);
    }

}
